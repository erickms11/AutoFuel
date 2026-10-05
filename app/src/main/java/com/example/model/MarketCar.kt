package com.example.model

enum class VehiclePowertrain(val label: String) {
    ELECTRIC("100% Elétrico"),
    HYBRID("Híbrido (HEV/PHEV)"),
    COMBUSTION("Combustão (Gasolina)")
}

data class MarketCarBenchmark(
    val id: String,
    val name: String,
    val category: String,
    val powertrain: VehiclePowertrain,
    val efficiencyKmL: Double = 0.0, // km/L for combustion/hybrid
    val efficiencyKmPerKwh: Double = 0.0, // km/kWh for electric
    val description: String,
    val badge: String = ""
) {
    // Calculate cost per kilometer in BRL (R$/km)
    fun calculateCostPerKm(gasolinePrice: Double, electricityPriceKwh: Double): Double {
        return when (powertrain) {
            VehiclePowertrain.ELECTRIC -> {
                if (efficiencyKmPerKwh > 0) electricityPriceKwh / efficiencyKmPerKwh else 0.12
            }
            VehiclePowertrain.HYBRID, VehiclePowertrain.COMBUSTION -> {
                if (efficiencyKmL > 0) gasolinePrice / efficiencyKmL else 0.40
            }
        }
    }

    fun getEfficiencyDisplay(): String {
        return when (powertrain) {
            VehiclePowertrain.ELECTRIC -> "${String.format(java.util.Locale("pt", "BR"), "%.1f", efficiencyKmPerKwh)} km/kWh (~${String.format(java.util.Locale("pt", "BR"), "%.1f", 100.0 / efficiencyKmPerKwh)} kWh/100km)"
            VehiclePowertrain.HYBRID, VehiclePowertrain.COMBUSTION -> "${String.format(java.util.Locale("pt", "BR"), "%.1f", efficiencyKmL)} km/L"
        }
    }

    companion object {
        val PRESET_CARS = listOf(
            // 1. 100% Elétricos
            MarketCarBenchmark(
                id = "dolphin_mini",
                name = "BYD Dolphin Mini (EV)",
                category = "100% Elétrico",
                powertrain = VehiclePowertrain.ELECTRIC,
                efficiencyKmPerKwh = 9.6, // ~10.4 kWh/100km
                description = "Compacto urbano elétrico mais vendido do Brasil. Altíssima eficiência energética.",
                badge = "Super Econômico"
            ),
            MarketCarBenchmark(
                id = "byd_dolphin",
                name = "BYD Dolphin GS (EV)",
                category = "100% Elétrico",
                powertrain = VehiclePowertrain.ELECTRIC,
                efficiencyKmPerKwh = 7.8, // ~12.8 kWh/100km
                description = "Hatch elétrico espaçoso com excelente relação custo-benefício.",
                badge = "Mais Popular"
            ),
            MarketCarBenchmark(
                id = "gwm_ora03",
                name = "GWM Ora 03 Skin (EV)",
                category = "100% Elétrico",
                powertrain = VehiclePowertrain.ELECTRIC,
                efficiencyKmPerKwh = 7.2, // ~13.8 kWh/100km
                description = "Hatch elétrico premium com 171 cv e visual retrô-futurista.",
                badge = "Desempenho"
            ),
            MarketCarBenchmark(
                id = "volvo_ex30",
                name = "Volvo EX30 (EV)",
                category = "100% Elétrico",
                powertrain = VehiclePowertrain.ELECTRIC,
                efficiencyKmPerKwh = 6.5, // ~15.3 kWh/100km
                description = "SUV compacto elétrico premium com alta potência e tecnologia de segurança.",
                badge = "SUV Premium"
            ),

            // 2. Híbridos
            MarketCarBenchmark(
                id = "corolla_hybrid",
                name = "Toyota Corolla Hybrid (HEV)",
                category = "Híbrido",
                powertrain = VehiclePowertrain.HYBRID,
                efficiencyKmL = 18.5,
                description = "Referência em eficiência na cidade combinando motor 1.8 a combustão com motor elétrico.",
                badge = "Alta Eficiência"
            ),
            MarketCarBenchmark(
                id = "byd_song_plus",
                name = "BYD Song Plus DM-i (PHEV)",
                category = "Híbrido",
                powertrain = VehiclePowertrain.HYBRID,
                efficiencyKmL = 25.0, // modo combinado híbrido plug-in
                description = "SUV híbrido plug-in com autonomia superior a 1.000 km combinados.",
                badge = "Plug-in"
            ),
            MarketCarBenchmark(
                id = "haval_h6_hev",
                name = "GWM Haval H6 HEV",
                category = "Híbrido",
                powertrain = VehiclePowertrain.HYBRID,
                efficiencyKmL = 13.8,
                description = "SUV médio híbrido convencional com 243 cv e bom espaço para a família.",
                badge = "SUV Médio"
            ),

            // 3. Carros a Combustão Mais Econômicos
            MarketCarBenchmark(
                id = "renault_kwid",
                name = "Renault Kwid 1.0",
                category = "Combustão Econômico",
                powertrain = VehiclePowertrain.COMBUSTION,
                efficiencyKmL = 15.5,
                description = "Um dos compactos a combustão mais econômicos do ranking do Inmetro (PBEV).",
                badge = "Inmetro Top"
            ),
            MarketCarBenchmark(
                id = "onix_plus",
                name = "Chevrolet Onix Plus 1.0",
                category = "Combustão Econômico",
                powertrain = VehiclePowertrain.COMBUSTION,
                efficiencyKmL = 15.2,
                description = "Sedan compacto com excelente aerodinâmica e baixo consumo na estrada (até 17,5 km/L).",
                badge = "Sedan Top"
            ),
            MarketCarBenchmark(
                id = "vw_polo_tsi",
                name = "Volkswagen Polo 170 TSI",
                category = "Combustão Econômico",
                powertrain = VehiclePowertrain.COMBUSTION,
                efficiencyKmL = 14.8,
                description = "Motor 1.0 Turbo com injeção direta que une bom torque e economia.",
                badge = "Turbo Eficiente"
            ),

            // 4. SUV Médio Padrão a Combustão (Para contraste de mercado)
            MarketCarBenchmark(
                id = "suv_medio_padrao",
                name = "SUV Médio Turbo Padrão",
                category = "SUV Médio (Combustão)",
                powertrain = VehiclePowertrain.COMBUSTION,
                efficiencyKmL = 11.0,
                description = "Média de consumo de SUVs médios turboflex (porte Compass/Corolla Cross 2.0/Taos).",
                badge = "Média de Mercado"
            )
        )
    }
}
