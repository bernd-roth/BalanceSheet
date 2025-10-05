package at.co.netconsulting.balancesheet.enums

enum class Position(val displayName: String, val category: PositionCategory) {
    // Rental-related positions
    mieteinkommen("Mieteinkommen", PositionCategory.RENTAL),
    wasser_heizung("Wasser/Heizung", PositionCategory.RENTAL),
    haushaltsversicherung("Haushaltsversicherung", PositionCategory.RENTAL),
    hausverwaltung("Hausverwaltung", PositionCategory.RENTAL),
    strom("Strom", PositionCategory.RENTAL),
    internet("Internet", PositionCategory.RENTAL),
    klimaanlage("Klimaanlage", PositionCategory.RENTAL),
    obs_haushaltsabgabe("OBS Haushaltsabgabe", PositionCategory.RENTAL),
    rechtsschutzversicherung("Rechtsschutzversicherung", PositionCategory.RENTAL),

    // Garage-related positions (Stipcakgasse)
    garage_a3_17("Garage A3/17", PositionCategory.GARAGE),
    garage_a1_12("Garage A1/12", PositionCategory.GARAGE),
    reparaturruecklage_garage_a3_17("Reparaturrücklage Garage A3/17", PositionCategory.GARAGE),
    reparaturruecklage_garage_a1_12("Reparaturrücklage Garage A1/12", PositionCategory.GARAGE),
    betriebskosten_garage_a3_17("Betriebskosten Garage A3/17", PositionCategory.GARAGE),
    betriebskosten_garage_a1_12("Betriebskosten Garage A1/12", PositionCategory.GARAGE),

    // Personal tax positions
    gehalt("Gehalt", PositionCategory.PERSONAL),
    essen("Essen", PositionCategory.PERSONAL),
    kurse("Kurse", PositionCategory.PERSONAL),
    literatur("Literatur", PositionCategory.PERSONAL),
    kammer("Kammer", PositionCategory.PERSONAL),
    gesundheit("Gesundheit", PositionCategory.PERSONAL),
    medizin("Medizin", PositionCategory.PERSONAL),
    arbeitssuche("Arbeitssuche", PositionCategory.PERSONAL),
    kleinmaterial("Kleinmaterial", PositionCategory.PERSONAL),
    sonderausgaben("Sonderausgaben", PositionCategory.PERSONAL),
    betriebsratsumlage("Betriebsratsumlage", PositionCategory.PERSONAL),
    wohnraumschaffung("Wohnraumschaffung", PositionCategory.PERSONAL),
    homeoffice("Homeoffice", PositionCategory.PERSONAL),
    steuerberater("Steuerberater", PositionCategory.PERSONAL),
    digitale_arbeitsmittel("Digitale Arbeitsmittel", PositionCategory.PERSONAL),
    laptop("Laptop", PositionCategory.PERSONAL),
    computer("Computer", PositionCategory.PERSONAL),
    telefon("Telefon", PositionCategory.PERSONAL),
    zusatzpension("Zusatzpension", PositionCategory.PERSONAL),
    auto("Auto", PositionCategory.PERSONAL),
    verkehrsmittel("Verkehrsmittel", PositionCategory.PERSONAL),
    bank("Bank", PositionCategory.PERSONAL);

    companion object {
        fun getByCategory(category: PositionCategory): List<Position> {
            return values().filter { it.category == category }
        }

        fun getForLocation(location: Location): List<Position> {
            return when (location) {
                Location.Hollgasse_1_1, Location.Hollgasse_1_54 -> getByCategory(PositionCategory.RENTAL)
                Location.Stipcakgasse_8_1 -> getByCategory(PositionCategory.GARAGE) + listOf(mieteinkommen)
                Location.Personal -> getByCategory(PositionCategory.PERSONAL)
            }
        }
    }
}

enum class PositionCategory {
    RENTAL,
    GARAGE,
    PERSONAL
}
