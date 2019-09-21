
import io.ktor.application.call
import io.ktor.response.respond
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
import io.ktor.features.origin
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.post
import java.time.Duration
// import java.time.LocalDateTime

class Servidor(private val puerto: Int) {
    private var etiqueta: String? = ""

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
                    val solicitud = jsonToSolicitud(objectoRecibido, false)

                    val jsonEnviar = contenidoPaginaJson( solicitud?.nombrePagina,
                        solicitud?.etiqueta, solicitud?.palabraClave )?.let { it1 -> objetoJson(it1) }

                    if( jsonEnviar != null && !jsonEnviar.isEmpty )
                        call.respond( TextContent( jsonEnviar.toString(), ContentType.Application.Json) )
                    else
                        call.respond( TextContent( "[]", ContentType.Application.Json) )
                    // println("Enlaces: " + LocalDateTime.now())
                }
                post("/guardarEnlaces"){

                    val objetoRecibidoString = call.receiveText()

                    val objetoRecibido = JSONObject(JSONObject(JSONArray(JSONObject(JSONObject(
                        objetoRecibidoString).get("params").toString()).
                        get("updates").toString()).
                        get(0).toString()).
                        get("value").toString())

                    val solicitud = jsonToSolicitud(objetoRecibido ,true)

                    if (solicitud != null) {
                        if (solicitud.dataFormatoDB() != "" && solicitud.elementosFormatoDB() != "") {
                            if(AlmacenTags().guardarEtiquetas(solicitud.nombrePagina, solicitud.dataFormatoDB(),
                                    solicitud.elementosFormatoDB(), call.request.origin.remoteHost)){
                                call.respondText { "1" }
                            } else {
                                call.respondText { "0" }
                            }
                        }
                    }
                    // println("GuardarEnlaces: " + LocalDateTime.now())
                }
            }
        }
        server.start(wait = true)
    }

    // Dados los parametros se obtienen tags de html
    private fun contenidoPaginaJson(nombrePagina: String?, etiqueta: String?, palabraClave: String? = ""):
            LinkedList<String>? {

        if( nombrePagina == "" || etiqueta == "")
            return null

        val doc: Document?
        try {
            doc = Jsoup.connect(nombrePagina).get()
            val listaEtiquetas = LinkedList<String>()

            if( doc != null ){
                // Ejemplo de formato de busqueda: div#logo:contains(jsoup)
                var etiquetaFinal = etiqueta

                if (palabraClave != "")
                    etiquetaFinal += ":contains($palabraClave)"

                doc.select(etiquetaFinal)?.forEach { elem -> listaEtiquetas.add(elem.toString()) }
                return listaEtiquetas
            }

        } catch (e: Exception){ }
        return null
    }

    // De lista de string a JSONObject
    private fun objetoJson(lista: LinkedList<String>): JSONArray?{
        if(!lista.isEmpty()){
            val listaEtiquetas = JSONArray()
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

    private fun jsonToSolicitud(objetoRecibido: JSONObject, conElementos: Boolean): Solicitud?{
        return if (conElementos) {
            Solicitud( objetoRecibido.get("nombreWeb").toString(),
                objetoRecibido.get("nombreEtiqueta").toString(),
                objetoRecibido.get("palabra").toString(),
                JSONArray(objetoRecibido.get("elementos").toString()))
        } else {
            Solicitud( objetoRecibido.get("nombreWeb").toString(),
                objetoRecibido.get("nombreEtiqueta").toString(),
                objetoRecibido.get("palabra").toString(),
                JSONArray())
        }
    }

}
// intercept(ApplicationCallPipeline.Call) {}
