import psycopg2
from openpyxl import Workbook
from openpyxl.styles import Font, Alignment, PatternFill, Border, Side
from datetime import datetime
from collections import defaultdict
import sys

# Database connection parameters
DB_CONFIG = {
    'host': 'HOSTNAME',
    'database': 'DATABASE_NAME',
    'user': 'USERNAME',
    'password': 'PASSWORD'
}

# Month names in German
MONTH_NAMES = {
    1: 'Jänner',
    2: 'Februar',
    3: 'März',
    4: 'April',
    5: 'Mai',
    6: 'Juni',
    7: 'Juli',
    8: 'August',
    9: 'September',
    10: 'Oktober',
    11: 'November',
    12: 'Dezember'
}

def get_tax_category_column(position, comment):
    """
    Map database position to Austrian tax category column
    Returns column name or None if not applicable
    """
    position_lower = position.lower().strip() if position else ''
    comment_lower = comment.lower().strip() if comment else ''
    
    # Exclude rental-related positions
    rental_positions = [
        'mieteinkommen', 'garage a3/17', 'garage a1/12',
        'reparaturrücklage garage a3/17', 'reparaturrücklage garage a1/12',
        'betriebskosten garage a3/17', 'betriebskosten garage a1/12',
        'wasser/heizung', 'haushaltsversicherung', 'hausverwaltung',
        'strom', 'internet', 'klimaanlage', 'obs haushaltsabgabe',
        'rechtsschutzversicherung'
    ]
    
    if position_lower in rental_positions:
        return None
    
    # Map positions to tax columns
    tax_mapping = {
        'arbeitssuche': 'arbeitssuche',
        'arbeitsmittel': 'anla_kleinmat',
        'auto': 'anla_kleinmat',
        'betriebsratsumlage': 'betriebsratsumlage',
        'bewerbung': 'arbeitssuche',
        'digitale arbeitsmittel': 'digitale arbeitsmittel',
        'fachliteratur': 'fachliteratur',
        'fortbildung': 'kurse',
        'gesundheit': 'gesundheit',
        'homeofficepauschale': 'homeofficepauschale',
        'kammer': 'kammer',
        'kleinmaterial': 'anla_kleinmat',
        'medizin': 'gesundheit',
        'sonderausgaben': 'sonderausgaben',
        'steuerberater': 'steuerberater',
        'verkehrsmittel': 'anla_kleinmat',
        'zustzpension': 'zusatzpension'
    }
    
    # Check if position matches any tax category
    for key, value in tax_mapping.items():
        if key in position_lower:
            return value
    
    # Check comment if position doesn't match
    for key, value in tax_mapping.items():
        if key in comment_lower:
            return value
    
    # Default category for non-rental, uncategorized items
    return 'sonderausgaben'


def get_database_data(person='Bernd', year=2025):
    """Fetch personal tax data from Postgres database"""
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    tax_category = f'{person.lower()}_private'
    
    query = """
        SELECT id, orderdate, position, income, expense, comment
        FROM incomeexpense
        WHERE tax_category = %s
        AND EXTRACT(YEAR FROM orderdate) = %s
        AND position NOT IN (
            'mieteinkommen', 'garage a3/17', 'garage a1/12',
            'reparaturrücklage garage a3/17', 'reparaturrücklage garage a1/12',
            'betriebskosten garage a3/17', 'betriebskosten garage a1/12',
            'wasser/heizung', 'haushaltsversicherung', 'hausverwaltung',
            'strom', 'internet', 'klimaanlage', 'obs haushaltsabgabe',
            'rechtsschutzversicherung'
        )
        ORDER BY orderdate, id
    """
    
    cursor.execute(query, (tax_category, year))
    rows = cursor.fetchall()
    
    cursor.close()
    conn.close()
    
    return rows


def aggregate_data_by_month(db_rows):
    """Aggregate entries by month and tax category"""
    monthly_data = defaultdict(lambda: defaultdict(lambda: {'amount': 0.0, 'entries': []}))

    for row in db_rows:
        db_id, orderdate, position, income, expense, comment = row

        category = get_tax_category_column(position, comment)
        if category is None:
            continue

        month = orderdate.month
        income_val = float(income) if income else 0.0
        expense_val = float(expense) if expense else 0.0

        # Determine the amount for ja/nein% and ansetzbar
        # Expenses get a negative sign
        if income_val != 0:
            amount = income_val
        elif expense_val != 0:
            amount = -expense_val  # Negative sign for expenses
        else:
            amount = 0.0

        monthly_data[month][category]['amount'] += amount
        monthly_data[month][category]['entries'].append({
            'date': orderdate,
            'description': position,
            'comment': comment if comment else '',
            'income': income_val,
            'expense': expense_val,
            'amount': amount
        })

    return monthly_data

def generate_excel(monthly_data, person='Bernd', year=2025, output_file=None):
    """Generate Excel file for Arbeitnehmerveranlagung"""
    
    if output_file is None:
        output_file = f'{person.lower()}_arbeitnehmerveranlagung_{year}.xlsx'
    
    wb = Workbook()
    ws = wb.active
    ws.title = f"{person}_ANV_{year}"
    
    # Define styles
    yellow_fill = PatternFill(start_color="FFFF00", end_color="FFFF00", fill_type="solid")
    centered_alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
    header_alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
    normal_alignment = Alignment(horizontal="left", vertical="center")
    
    thin_border = Border(
        left=Side(style='thin'),
        right=Side(style='thin'),
        top=Side(style='thin'),
        bottom=Side(style='thin')
    )
    
    # Row 1: Title with yellow background
    ws.merge_cells('A1:U1')
    title_cell = ws['A1']
    title_cell.value = f"Arbeitnehmerveranlagung {person}\nfür das Jahr {year}"
    title_cell.fill = yellow_fill
    title_cell.alignment = centered_alignment
    title_cell.font = Font(bold=True, size=12)
    ws.row_dimensions[1].height = 45
    
    # Row 2: Column headers
    headers = [
        'rechnungsnummer',
        'datum',
        'artikelbeschreibung',
        'ja/nein',
        '%prozent',
        'ansetzbar',
        'kurse',
        'literatur',
        'kammer',
        'gesundheit',
        'arbeitssuche',
        'anla/\nkleinmat',
        'sonder-\nausgaben',
        'strom',
        'betriebs-\nrats-\numlage',
        'wohnraum-\nschaffung',
        'homeOffice\nPauschale',
        'steuer-\nberater',
        'digitale\narbe its-\nmittel',
        'zustz-\npension',
        'comment'
    ]
    
    for col_idx, header in enumerate(headers, start=1):
        cell = ws.cell(row=2, column=col_idx)
        cell.value = header
        cell.alignment = header_alignment
        cell.font = Font(bold=True, size=9)
        cell.border = thin_border
    
    ws.row_dimensions[2].height = 45
    
    # Freeze first 2 rows
    ws.freeze_panes = 'A3'
    
    # Set column widths
    column_widths = {
        'A': 18,  # rechnungsnummer
        'B': 12,  # datum
        'C': 25,  # artikelbeschreibung
        'D': 8,   # ja/nein
        'E': 10,  # %prozent
        'F': 12,  # ansetzbar
        'G': 10,  # kurse
        'H': 10,  # literatur
        'I': 10,  # kammer
        'J': 10,  # gesundheit
        'K': 10,  # arbeitssuche
        'L': 10,  # anla/kleinmat
        'M': 10,  # sonderausgaben
        'N': 10,  # strom
        'O': 10,  # betriebsratsumlage
        'P': 10,  # wohnraumschaffung
        'Q': 10,  # homeOffice
        'R': 10,  # steuerberater
        'S': 10,  # digitale arbeitsmittel
        'T': 10,  # zusatzpension
        'U': 30,  # comment
    }
    
    for col, width in column_widths.items():
        ws.column_dimensions[col].width = width
    
    # Initialize totals
    totals = {
        'ansetzbar': 0.0,
        'kurse': 0.0,
        'literatur': 0.0,
        'kammer': 0.0,
        'gesundheit': 0.0,
        'arbeitssuche': 0.0,
        'anla_kleinmat': 0.0,
        'sonderausgaben': 0.0,
        'strom': 0.0,
        'betriebsratsumlage': 0.0,
        'homeoffice_pauschale': 0.0,
        'steuerberater': 0.0,
        'digitale_arbeitsmittel': 0.0,
        'zusatzpension': 0.0,
    }
    
    current_row = 3
    
    # Column mapping - 1:1 mapping between position name and column
    # Position name (lowercase, with underscores) maps directly to column
    position_to_column = {
        'kurse': 7,                        # G
        'fachliteratur': 8,                # H
        'kammer': 9,                       # I
        'gesundheit': 10,                  # J
        'arbeitssuche': 11,                # K
        'kleinmaterial': 12,               # L (anla/kleinmat)
        'auto': 12,                        # L
        'verkehrsmittel': 12,              # L
        'sonderausgaben': 13,              # M
        'strom': 14,                       # N
        'betriebsratsumlage': 15,          # O
        'versicherung': 16,                # P (wohnraumschaffung)
        'homeoffice': 17,                  # Q
        'steuerberater': 18,               # R
        'digitale_arbeitsmittel': 19,      # S
        'telefon': 19,                     # S
        'zusatzpension': 20,               # T
        'medizin': 10,                     # J (gesundheit)
    }

    invoice_number = 1
    
    # Write data for each month
    for month in range(1, 13):
        month_name = MONTH_NAMES[month]
        
        categories_in_month = monthly_data.get(month, {})
        
        if not categories_in_month:
            continue
        
        # Month header row
        ws.cell(row=current_row, column=2).value = month_name
        ws.cell(row=current_row, column=2).font = Font(bold=True)
        current_row += 1

        # Write entries
        for category, data in categories_in_month.items():
            for entry in data['entries']:
                # Get position name for 1:1 mapping
                position_name = entry['description'].lower().replace(' ', '_')

                # Rechnungsnummer
                invoice_cell = ws.cell(row=current_row, column=1)
                invoice_cell.value = invoice_number
                invoice_cell.number_format = '000'
                invoice_number += 1

                # Date
                date_cell = ws.cell(row=current_row, column=2)
                date_cell.value = entry['date']
                date_cell.number_format = 'dd.mm.yyyy'

                # Artikelbeschreibung (position from DB)
                ws.cell(row=current_row, column=3).value = entry['description']

                # ja/nein% - income or expense value (or 0 if both are 0)
                ja_nein_cell = ws.cell(row=current_row, column=4)
                ja_nein_cell.value = entry['amount']
                ja_nein_cell.number_format = '#,##0.00 [$€-1]'

                # %prozent - leave empty for manual entry
                ws.cell(row=current_row, column=5).value = ''

                # ansetzbar - same as ja/nein%
                ansetzbar_cell = ws.cell(row=current_row, column=6)
                ansetzbar_cell.value = entry['amount']
                ansetzbar_cell.number_format = '#,##0.00 [$€-1]'
                totals['ansetzbar'] += entry['amount']

                # Map to specific column based on position name (1:1 mapping)
                if position_name in position_to_column:
                    col_idx = position_to_column[position_name]
                    amount_cell = ws.cell(row=current_row, column=col_idx)
                    amount_cell.value = entry['amount']
                    amount_cell.number_format = '#,##0.00 [$€-1]'

                    # Update totals using the position name as key
                    if position_name not in totals:
                        totals[position_name] = 0.0
                    totals[position_name] += entry['amount']

                # Comment (column U = 21)
                ws.cell(row=current_row, column=21).value = entry.get('comment', '')

                current_row += 1
    
    # Add totals row
    ws.cell(row=current_row, column=2).value = "SUMME"
    ws.cell(row=current_row, column=2).font = Font(bold=True)

    # Total ansetzbar
    total_cell = ws.cell(row=current_row, column=6)
    total_cell.value = totals['ansetzbar']
    total_cell.number_format = '#,##0.00 [$€-1]'
    total_cell.font = Font(bold=True)
    
    # Position totals (1:1 mapping)
    for position_name, col_idx in position_to_column.items():
        if position_name in totals and totals[position_name] != 0:
            total_cat_cell = ws.cell(row=current_row, column=col_idx)
            total_cat_cell.value = totals[position_name]
            total_cat_cell.number_format = '#,##0.00 [$€-1]'
            total_cat_cell.font = Font(bold=True)
    
    wb.save(output_file)
    return output_file

def main():
    """Main function to export Arbeitnehmerveranlagung data"""
    try:
        # Get person from command line (Bernd or Julia)
        if len(sys.argv) < 2:
            print("Usage: python3 arbeitnehmerveranlagung.py <person> [year]")
            print("Example: python3 arbeitnehmerveranlagung.py Bernd")
            print("Example: python3 arbeitnehmerveranlagung.py Julia 2025")
            return
        
        person = sys.argv[1].capitalize()
        
        if person not in ['Bernd', 'Julia']:
            print(f"Invalid person: {person}. Must be 'Bernd' or 'Julia'")
            return
        
        # Get year from command line or use current year
        if len(sys.argv) > 2:
            try:
                year = int(sys.argv[2])
            except ValueError:
                print(f"Invalid year: {sys.argv[2]}")
                return
        else:
            year = datetime.now().year
        
        print(f"Generating Arbeitnehmerveranlagung for: {person}, year: {year}")
        print(f"Fetching data from database...")
        db_rows = get_database_data(person=person, year=year)
        
        if not db_rows:
            print(f"No personal tax data found for {person} in {year}")
            return
        
        print(f"Found {len(db_rows)} total records")
        
        print("Aggregating tax data by month and category...")
        monthly_data = aggregate_data_by_month(db_rows)
        
        tax_entries = sum(len(cat['entries']) for month_data in monthly_data.values() 
                         for cat in month_data.values())
        print(f"Found {tax_entries} tax-relevant entries")
        
        print("\nMonthly breakdown:")
        for month in sorted(monthly_data.keys()):
            month_name = MONTH_NAMES.get(month, f"Month {month}")
            total_month = sum(cat['amount'] for cat in monthly_data[month].values())
            print(f"  {month_name}: Total: {abs(total_month):.2f} €")
        
        output_file = f'{person.lower()}_arbeitnehmerveranlagung_{year}.xlsx'
        print(f"\nGenerating Excel file: {output_file}")
        generate_excel(monthly_data, person=person, year=year, output_file=output_file)
        
        print(f"✓ Excel file created successfully: {output_file}")
        print(f"\nTo download from container:")
        print(f"docker cp $(docker-compose ps -q web):/app/{output_file} ./exports/{output_file}")
        
    except Exception as e:
        print(f"Error: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()