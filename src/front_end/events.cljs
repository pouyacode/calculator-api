(ns front-end.events
  (:require
   [re-frame.core :as re-frame]
   [front-end.db :as db]
   [ajax.core :as ajax]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::navigate
 (fn [_ [_ handler]]
   {:navigate handler}))

(re-frame/reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)}))

;;; Update user's input in `db`.
(re-frame/reg-event-fx
 ::expression
 (fn [{:keys [db]} [_ expression]]
   {:db (assoc db :expression expression)}))

;;; Send `POST` request to server and retrieve the result of calculation.
(re-frame/reg-event-fx
 ::calc
 (fn [{:keys [db]} _]
   {:db (assoc db :loading "block")
    :http-xhrio {:method :post
                 :uri "/calc"
                 :timeout 8000
                 :params (js/JSON.stringify (clj->js {:expression (:expression db)}))
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::good-post-result]
                 :on-failure [::bad-post-result]}}))

;;; Send `POST` request to retrieve the history of successful calculations.
(re-frame/reg-event-fx
 ::hist
 (fn [{:keys [db]} _]
   {:db (assoc db :loading "block")
    :http-xhrio {:method :post
                 :uri "/hist"
                 :timeout 8000
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format)
                 :on-success [::good-post-result]
                 :on-failure [::bad-post-result]}}))

;;; Send `POST` request to retrieve the debug info of calulation.
(re-frame/reg-event-fx
 ::dbug
 (fn [{:keys [db]} _]
   {:db (assoc db :loading "block")
    :http-xhrio {:method :post
                 :uri "/dbug"
                 :timeout 8000
                 :params (js/JSON.stringify (clj->js {:expression (:expression db)}))
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::good-post-result]
                 :on-failure [::bad-post-result]}}))

;;; Put the result from server, into `db`
(re-frame/reg-event-fx
 ::good-post-result
 (fn [{:keys [db]} [_ value]]
   {:db (-> db
            (assoc :loading "none")
            (assoc :result (js/JSON.stringify (clj->js value) nil 2)))}))

;;; Call `::calc` if user press `Enter` on the `input` element.
(re-frame/reg-event-fx
 ::enter
 (fn [{:keys [db]} [_ event]]
   (if (= 13 (.-keyCode event))
     (re-frame/dispatch [::calc]))))
