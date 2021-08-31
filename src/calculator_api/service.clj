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
                         [:meta {:charset "utf-8"}]
                         [:title "Calculator API"]
                         [:meta {:name "description"
                                 :content "Basic arithmetic calculator, using Antlr4."}]
                         [:meta {:name "viewport"
                                 :content "width=device-width,minimum-scale=1,initial-scale=1"}]
                         [:link {:rel "stylesheet"
                                 :href "/vendor/css/style.min.css?v0"}]
                         [:link {:rel "stylesheet"
                                 :href "/vendor/css/highlight.min.css?v0"}]
                         [:script {:src "/js/compiled/app.js?v0"
                                   :defer "true"}]
                         [:script {:src "/js/highlight.min.js?v0"
                                   :defer "true"}]
                         [:link {:rel "icon"
                                 :type "image/svg+xml"
                                 :href "/favicon.svg"}]
                         [:meta {:property "og:locale"
                                 :content "en_GB"}]
                         [:meta {:property "og:type"
                                 :content "website"}]
                         [:meta {:property "og:title"
                                 :content "Calculator API"}]
                         [:meta {:property "og:description"
                                 :content "Calculator RESTful API, written in clojure, using Antlr4 parser generator."}]
                         [:meta {:property "og:url"
                                 :content "https://ardoq.pouyacode.net"}]
                         [:meta {:property "og:image"
                                 :content "https://ardoq.pouyacode.net/calculator.png"}]
                         [:meta {:property "og:site_name"
                                 :content "Calculator API"}]]
                        [:body
                         [:div#app
                          [:noscript
                           [:div.noscript]
                           [:pre#description "It's a JS app, Please enable JavaScript to continue."
                            [:br]
                            "For more information, May the "
                            [:a {:href "/docs" :target "_blank"} "Source"]
                            "Be With You!"]]]]])))


(defn docs
  "Load the document generated via `marginalia`
  https://github.com/gdeer81/marginalia. And serve it in /docs. It looks like
  Literate Programming!
  
  Use `lein marg` to update the documents whenever
  you changed some code or comment."
  [_]
  (ring-resp/response (slurp (clojure.java.io/resource "uberdoc.html"))))


(defn de-json
  "Extracts `json` part of the request. Returns `nil` if `content-type` isn't
  set properly or `json-params` doesn't exist in request body, or there's no
  `expression` key in the JSON."
  [request]
  (let [content-type ((:headers request) "content-type")]
    (if (or (= "application/json; charset=UTF-8" content-type)
            (= "application/json" content-type))
      (if-let [json-params (:json-params request)]
        (-> json-params
            (json/read-str :key-fn #(keyword %))
            :expression))
      nil)))


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
              ::http/routes routes
              ::http/allowed-origins ["http://localhost:8080"
                                      "https://ardoq.pouyacode.net"]
              ::http/secure-headers {:content-security-policy-settings
                                     {:object-src "'none'"
                                      :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
                                      :frame-ancestors "'none'"}}
              ::http/resource-path "/public" ; resources/public/...
              ::http/join?  false
              ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}
              ::http/type :jetty
              ::http/host "localhost"
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false
                                        }})
