(ns front-end.views
  (:require
   [re-frame.core :as re-frame]
   [front-end.events :as events]
   [front-end.routes :as routes]
   [front-end.subs :as subs]))

;; home

(defn home-title []
  (let [result @(re-frame/subscribe [::subs/result])]
    [:div#outer
     [:div.container
      [:input#expression {:type "text"
                          :placeholder "Expression"
                          :name "expression"
                          :on-input #(re-frame/dispatch
                                      [::events/expression
                                       (-> % .-target .-value)])
                          :on-key-up #(re-frame/dispatch [::events/enter %])}]
      [:input#calculate {:type "submit"
                         :value "Calculate"
                         :on-click #(re-frame/dispatch [::events/calc])}]
      [:input#history {:type "submit"
                       :value "History"
                       :on-click #(re-frame/dispatch [::events/hist])}]
      [:input#debug {:type "submit"
                     :value "Debug"
                     :on-click #(re-frame/dispatch [::events/dbug])}]]
     [:div#inner
      [:pre
       [:code#result {:class "language-json"
                      :ref (fn [n]      ; when changed, call `highlightjs`
                             (when n (js/setTimeout
                                      #(js/hljs.highlightBlock n) 0)))}
        result]]]]))


(defn link-to-docs []
  (let [loading @(re-frame/subscribe [::subs/loading])]
    [:div
     [:pre#description "For more information, May The "
      [:a {:href "/docs" :target "_blank"} "Source"]
      #_[:button.source
       {:on-click #(re-frame/dispatch [::events/navigate :docs])}
       "Source"]
      " Be With You!"]
     [:div#loading                      ; Loading animation on bottom-left
      {:style {:display loading}}]]))


(defn fork-me []
  [:div#fork
   [:a {:href "https://github.com/pouyacode/calculator-api" :target "_blank"}
    "Fork me on GitHub!"]])


(defn home-panel []
  [:div
   [home-title]
   [link-to-docs]
   [fork-me]])


(defmethod routes/panels :home-panel [] [home-panel])

;; about

(defn about-title []
  #_[re-com/title
     :src   (at)
     :label "This is the About Page."
     :level :level1])

(defn link-to-home-page []
  #_[re-com/hyperlink
     :src      (at)
     :label    "go to Home Page"
     :on-click #(re-frame/dispatch [::events/navigate :home])])

(defn docs-panel []
  [:button
   {:on-click #(re-frame/dispatch [::events/navigate :home])}
   "Home"]
  #_[re-com/v-box
     :src      (at)
     :gap      "1em"
     :children [[about-title]
                [link-to-home-page]]])

(defmethod routes/panels :docs-panel [] [docs-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    (routes/panels @active-panel)))
