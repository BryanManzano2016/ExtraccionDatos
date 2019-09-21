import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager

class AlmacenTags(
    private val url: String = "jdbc:postgresql://localhost:9001/AlmacenTags",
    private val user: String = "postgres",
    private val password: String = "root"
) {

    private fun conexion(): Connection? {
        return DriverManager.getConnection(this.url, this.user, this.password)
    }

    fun guardarEtiquetas(webKt: String, dataKt: String, elementosGuardarKt: String,
                         ip: String): Boolean {
        try {
            val procedimiento: CallableStatement? = conexion()?.prepareCall("CALL insertarTag( ?, ?, ?, ? )") ?: return false

            procedimiento?.setString(1, webKt)
            procedimiento?.setString(2, dataKt)
            procedimiento?.setString(3, elementosGuardarKt)
            procedimiento?.setString(4, ip)

            procedimiento?.execute()
        } catch (e: Exception) {
            return false
        }
        return true
    }
}