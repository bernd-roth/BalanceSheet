package at.co.netconsulting.balancesheet.enums

enum class Position(val displayName: String, val category: PositionCategory) {
    // Rental-related positions
    haushaltsversicherung("Haushaltsversicherung", PositionCategory.RENTAL),
    hausverwaltung("Hausverwaltung", PositionCategory.RENTAL),
    internet("Internet", PositionCategory.RENTAL),
    klimaanlage("Klimaanlage", PositionCategory.RENTAL),
    mieteinkommen("Mieteinkommen", PositionCategory.RENTAL),
    obs_haushaltsabgabe("OBS Haushaltsabgabe", PositionCategory.RENTAL),
    rechtsschutzversicherung("Rechtsschutzversicherung", PositionCategory.RENTAL),
    strom("Strom", PositionCategory.RENTAL),
    wasser_heizung("Wasser/Heizung", PositionCategory.RENTAL),

    // Garage-related positions (Stipcakgasse)
    garage_a1_12("Garage A1/12", PositionCategory.GARAGE),
    garage_a3_17("Garage A3/17", PositionCategory.GARAGE),
    reparaturruecklage_garage_a1_12("Reparaturrücklage A1/12", PositionCategory.GARAGE),
    reparaturruecklage_garage_a3_17("Reparaturrücklage A3/17", PositionCategory.GARAGE),
    betriebskosten_garage_a1_12("Betriebskosten A1/12", PositionCategory.GARAGE),
    betriebskosten_garage_a3_17("Betriebskosten A3/17", PositionCategory.GARAGE),

    // Personal tax positions
    auto("Auto", PositionCategory.PERSONAL),
    arbeitssuche("Arbeitssuche", PositionCategory.PERSONAL),
    bank("Bank", PositionCategory.PERSONAL),
    betriebsratsumlage("Betriebsratsumlage", PositionCategory.PERSONAL),
    digitale_arbeitsmittel("Digitale Arbeitsmittel", PositionCategory.PERSONAL),
    essen("Essen", PositionCategory.PERSONAL),
    gehalt("Gehalt", PositionCategory.PERSONAL),
    gesundheit("Gesundheit", PositionCategory.PERSONAL),
    homeoffice("Homeoffice", PositionCategory.PERSONAL),
    kammer("Kammer", PositionCategory.PERSONAL),
    kleinmaterial("Kleinmaterial", PositionCategory.PERSONAL),
    kurse("Kurse", PositionCategory.PERSONAL),
    fachliteratur("Fachliteratur", PositionCategory.PERSONAL),
    medizin("Medizin", PositionCategory.PERSONAL),
    sonderausgaben("Sonderausgaben", PositionCategory.PERSONAL),
    steuerberater("Steuerberater", PositionCategory.PERSONAL),
    telefon("Telefon", PositionCategory.PERSONAL),
    verkehrsmittel("Verkehrsmittel", PositionCategory.PERSONAL),
    versicherung("Versicherung", PositionCategory.PERSONAL),
    zusatzpension("Zusatzpension", PositionCategory.PERSONAL);

    companion object {
        fun getByCategory(category: PositionCategory): List<Position> {
            return values().filter { it.category == category }
        }

        fun getForLocation(location: Location): List<Position> {
            return when (location) {
                Location.Hollgasse_1_1, Location.Hollgasse_1_54 -> getByCategory(PositionCategory.RENTAL)
                Location.Stipcakgasse_8 -> getByCategory(PositionCategory.GARAGE) + listOf(mieteinkommen)
                Location.Personal -> getByCategory(PositionCategory.PERSONAL)
            }
        }

        fun getForLocationAndTaxCategory(location: Location, taxCategory: TaxCategory): List<Position> {
            return when (location) {
                Location.Hollgasse_1_1 -> {
                    // For Hollgasse 1/1 with Gemeinsam, include bank position
                    if (taxCategory == TaxCategory.gemeinsam) {
                        getByCategory(PositionCategory.RENTAL) + listOf(bank)
                    } else {
                        getByCategory(PositionCategory.RENTAL)
                    }
                }
                Location.Hollgasse_1_54 -> getByCategory(PositionCategory.RENTAL)
                Location.Stipcakgasse_8 -> getByCategory(PositionCategory.GARAGE) + listOf(mieteinkommen)
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
