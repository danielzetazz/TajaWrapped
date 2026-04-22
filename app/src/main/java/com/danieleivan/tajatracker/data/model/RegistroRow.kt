package com.danieleivan.tajatracker.data.model

data class RegistroRow(
    val id: String,
    val fechaHora: String? = null,
    val lugarNombre: String = "",
    val fotoUri: String? = null,
    val cubatasHidalgoTotal: Int = 0,
    val vomitosTotal: Int = 0
)

