import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.DefaultHeaders
import io.ktor.features.origin
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.request.*
import org.json.JSONML
import java.time.Duration

class Servidor(puerto: Int) {
    private val puerto= puerto
    private var etiqueta: String = ""

    init {
        ejecucionServidor()
    }
    // Responde a los request del cliente
    private fun ejecucionServidor() {
        val server = embeddedServer(Netty, port = puerto) {
            install(CORS) {
                method(HttpMethod.Options)
                header(HttpHeaders.XForwardedProto)
                anyHost()
                host("localhost:4200", schemes = listOf("http", "https"))
                allowCredentials = true
                allowNonSimpleContentTypes = true
                maxAge = Duration.ofDays(1)
            }
            routing {
                get("/enlaces") {
                    val objectoRecibido = JSONObject(call.request.queryParameters["solicitud"])

                    val solicitud = Solicitud( objectoRecibido.get("nombreWeb").toString(),
                        objectoRecibido.get("nombreEtiqueta").toString(), objectoRecibido.get("identificador").toString(),
                        objectoRecibido.get("palabra").toString() )

                    val jsonEnviar =
                        contenidoPaginaJson(solicitud.nombrePagina, solicitud.etiqueta, solicitud.id, solicitud.palabraClave)?.let { it1 ->
                            objetoJson(
                                it1
                            )
                        }

                    if( jsonEnviar != null)
                        call.respond( TextContent( jsonEnviar.toString(), ContentType.Application.Json) )
                    else
                        call.respond( TextContent( "[{}]", ContentType.Application.Json) )

                    println("*")
                    println(objectoRecibido.toString())
                }
            }
        }
        server.start(wait = true)
    }

    // Da formato a los mensajes del cliente
    data class Solicitud(val nombrePagina: String, val etiqueta: String,
                         val id: String = "", val palabraClave: String = "")

    // Dados los parametros se obtienen tags de html
    private fun contenidoPaginaJson(nombrePagina: String, etiqueta: String, id: String = "",
                                    palabraClave: String = ""): LinkedList<String>? {
        val doc: Document?
        try {
            doc = Jsoup.connect(nombrePagina).get()
            val listaEtiquetas = LinkedList<String>()
            if( doc != null ){
                // Ejemplo de formato de busqueda: div#logo:contains(jsoup)
                var etiquetaFinal = etiqueta
                this.etiqueta = etiquetaFinal

                if (id != "")
                    etiquetaFinal += "#$id"
                if (palabraClave != "")
                    etiquetaFinal += ":contains($palabraClave)"

                doc.select(etiqueta)?.forEach { elem -> listaEtiquetas.add(elem.toString()) }
                return listaEtiquetas
            }
        } catch (e: Exception){ println("Pagina no valida")  }
        return null
    }

    // De lista de string a JSONObject
    private fun objetoJson(lista: LinkedList<String>): JSONArray?{
        if(!lista.isEmpty()){
            var listaEtiquetas = JSONArray()
            lista.forEach{ elem ->
                run {
                    val objeto = JSONObject().also {
                        it.put("etiqueta", this.etiqueta)
                        it.put("elemento", elem)
                    }
                    listaEtiquetas.put(objeto)
                }
            }
            this.etiqueta = ""
            return listaEtiquetas
        }
        return null
    }

}

/*
intercept(ApplicationCallPipeline.Call) {
    println(a++)
    println("---")
    call.respond(TextContent( objetoJson(contenidoPaginaJson("https://example.com", "p",
        "", "")).toString(),
        ContentType.Application.Json))
}
*/
