import org.json.JSONArray

class Solicitud( val nombrePagina: String, val etiqueta: String, val palabraClave: String = "",
    private val elementos: JSONArray ) {

    fun dataFormatoDB(): String {
        return if ( etiqueta != "" && palabraClave == "" )
            "{$etiqueta,''}"
        else if ( etiqueta != "" && palabraClave != "" )
            "{$etiqueta,$palabraClave}"
        else
            ""
    }

    fun elementosFormatoDB(): String {
        if (!elementos.isEmpty) {
            var cadena = ""
            elementos.forEach { elemento -> run { cadena += "$elemento," } }
            return cadena.substring(0, cadena.length - 1)
        }
        return ""
    }

    override fun toString(): String {
        return "Solicitud(nombrePagina='$nombrePagina', etiqueta='$etiqueta', palabraClave='$palabraClave', elementos=$elementos)"
    }
}