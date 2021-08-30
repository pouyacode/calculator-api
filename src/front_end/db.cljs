(ns front-end.db)


(def default-db
  {:expression "-1 * (2 * 6 / 3)"       ; The expression typed by user.
   :result ""                           ; Result of calculation, from back-end.
   :loading "none"})                    ; `display` attribute of loading anim.
