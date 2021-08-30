(ns front-end.subs
  (:require
   [re-frame.core :as re-frame]))


;;; Get active-panel.
;;; Right now it's useless, but in the future, there'll be a panel for docs.
(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))


;;; Result of calculations, sent from back-end.
(re-frame/reg-sub
 ::result
 (fn [db _]
   (:result db)))


;;; The `display` attribute of the `loading` animation on bottom-left corner.
;;; It should be either `block` or `none`.
(re-frame/reg-sub
 ::loading
 (fn [db _]
   (:loading db)))
