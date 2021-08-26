(ns calculator-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.core :as h]
            [clojure.data.json :as json]
            [calculator-api.calculator :as calculator]))


(defn home-page
  "Generate the content of `/` page.

  With a simple JS/CSS syntax highlighting, it serves as a simple test suit for
  our REST API; Offering an input to write your math expression, three buttons
  to call `/calc` (process the expression), `/hist` (history of successful
  calculations), `/dbug` (same as `/calc` but return the intermediate state of
  the process).

  Why not just load an HTML from hard-disk? Because `hiccup` is way cooler B-)"
  [_]
  (ring-resp/response (h/html
                       "<!DOCTYPE html>"
                       [:html {:lang "en"}
                        [:head
                         [:title "Calculator API"]
                         [:meta {:name "description" :content "Basic arithmetic calculator, using Antlr4."}]
                         [:link {:rel "stylesheet" :href "/style/dark.min.css"}]
                         [:script {:src "/script/main.js" :defer "true"}]
                         [:script {:src "/script/highlight.min.js" :defer "true"}]
                         [:link {:rel "icon" :type "image/svg+xml" :href "/favicon.svg"}]]
                        [:body
                         [:div#outer
                          [:div.container
                           [:input#expression {:type "text"
                                               :placeholder "Expression"
                                               :name "expression"
                                               :value "-1 * (2 * 6 / 3)"}]
                           [:input#calculate {:type "submit"
                                              :value "Calculate"}]
                           [:input#history {:type "submit"
                                            :value "History"}]
                           [:input#debug {:type "submit"
                                          :value "Debug"}]]
                          [:div#inner
                           [:pre
                            [:code#result {:class "language-json"}]]]]
                         [:pre#description "For more information, May The "
                          [:a {:href "/docs" :target "_blank"} "Source"]
                          " Be With You!"]]])))


(defn docs
  "Load the document generated via `marginalia`
  https://github.com/gdeer81/marginalia. And serve it in /docs. It looks like
  Literate Programming!
  
  Use `lein marg -f ../resources/index.html` to update the documents whenever
  you changed some code or comment."
  [_]
  (ring-resp/response (slurp "resources/index.html")))


(defn de-json
  "Extracts `json` part of the request. Returns `nil` if `content-type` isn't
  set properly or `json-params` doesn't exist in request body, or there's no
  `expression` key in the JSON."
  [request]
  (if (= "application/json; charset=UTF-8" ((:headers request) "content-type"))
    (if-let [json-params (:json-params request)]
      (-> json-params
          (json/read-str :key-fn #(keyword %))
          :expression))
    nil))


(defn calc
  "Invoke `calculator-api.calculator/calc` to process the math expression.
  Return the response in JSON format, or return an error if the expression does
  not pass the validation step (on `calculator` namespace)."
  [request]
  (if-let [expression (de-json request)]
    (-> expression
        calculator/calc
        json/write-str
        ring-resp/response)
    (-> {:error "Parsing JSON"}
        json/write-str
        ring-resp/response)))


(defn hist
  "Retrieve the history of calculations from `database` atom, and return it as
  a JSON object.
  I didn't create a way to erase or otherwise manipulate history, I didn't think
  it's necessary."
  [_]
  (-> calculator/history
      deref
      json/write-str
      ring-resp/response))


(defn dbug
  "Roughly same as `calc` function, but uses `calculator-api.calculator/dbug`
  to produce the intermediate step of the process. Only for debuging purpose."
  [request]
  (if-let [expression (de-json request)]
    (-> expression
        calculator/dbug
        json/write-str
        ring-resp/response)
    (-> {:error "Parsing JSON"}
        json/write-str
        ring-resp/response)))


;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/docs).
(def common-interceptors [(body-params/body-params) http/html-body])


;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/calc" :post (conj common-interceptors `calc)]
              ["/dbug" :post (conj common-interceptors `dbug)]
              ["/hist" :post (conj common-interceptors `hist)]
              ["/docs" :get (conj common-interceptors `docs)]})


;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by calculator-api.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]
              ::http/allowed-origins ["http://localhost:8080"
                                      "https://ardoq.pouyacode.net"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public" ; resources/public/...

              ::http/join?  false
              ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}
              
              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/host "0.0.0.0"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false
                                        ;; Alternatively, You can specify you're own Jetty HTTPConfiguration
                                        ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)
                                        }})
