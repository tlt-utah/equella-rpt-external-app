package equella.rpt

import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPut
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

class ApiController {

    def put() {

        def equellaRestApiUri = grailsApplication.config.equella.rpt.rest.uri
        def equellaOauthToken = grailsApplication.config.equella.rpt.oauth.token

        def info = "[${new Date()}] Method: ${params.get('method')} ... "

        // get item's UUID/Version and call PUT API to trigger Save Script.
        def itemList = params.get('itemList')
        
        if (itemList) {

            Thread.start {

                println "[${new Date()}] PUT requests begin ..."

                CloseableHttpClient httpClient = HttpClients.custom().setHostnameVerifier(new AllowAllHostnameVerifier()).build()

                try {

                    "${itemList}".split(',').eachWithIndex { item, i ->

                        if (i == 0)
                            sleep 3000L

                        def (itemUuid, itemVersion) = item.tokenize('_')

                        HttpPut httpPut = new HttpPut(equellaRestApiUri + "/api/item/${itemUuid}/${itemVersion}")
                        httpPut.addHeader("X-Authorization", "access_token=${equellaOauthToken}")
                        httpPut.addHeader("Content-Type", "application/json")
                        httpPut.entity = new StringEntity(JsonOutput.toJson([uuid: itemUuid, version: itemVersion]))
                        CloseableHttpResponse response = httpClient.execute(httpPut)

                        try {
                            println "[${new Date()}] ${httpPut.getRequestLine()} >>> ${response.statusLine.toString()}"
                        } finally {
                            response.close()
                        }
                    }

                } finally {
                    httpClient.close()
                }
            }

        } else {
            info += "EXCEPTION: 'itemList' parameter NOT found."
        }

        println info
        render "${info}"
    }
}
