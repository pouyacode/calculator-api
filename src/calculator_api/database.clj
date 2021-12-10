(ns calculator-api.database
  (:require [clojure.java.jdbc :as db]
            [ragtime.jdbc :as rag-db]
            [ragtime.repl :as rag-repl]))


;; Postgresql connection map.
(def pg-db {:classname    "org.postgresql.Driver"
            :subprotocol  "postgresql"
            :subname      "//localhost:5432/thedbitself"
            :user         "thedbuser"
            :password     "thedbpassword"})

(def migrate-config
  {:datastore  (rag-db/sql-database pg-db)
   :migrations (rag-db/load-resources "migrations")})


(defn migrate []
  (rag-repl/migrate migrate-config))


(defn rollback []
  (rag-repl/rollback migrate-config))


(defn health-check
  "Make sure database is working. If not, return error.
  Run migrations to make sure everything is up-to-date."
  []
  (try
    (try
      (db/query pg-db ["SELECT datname FROM pg_database;"]) ; Should always work.
      (migrate))
    (catch Exception e
      (do (println e)
          (java.lang.System/exit 1)))))


(defn insert
  "insert successful calculations in database."
  [expression result]
  (db/insert! pg-db
              :history
              {:expression expression
               :result result}))


(defn history
  "query all recorded calculations."
  []
  (let [return (map #(dissoc % :id) (db/query pg-db ["SELECT * FROM history"]))
        freq (frequencies return)]
    (map #(assoc (key %) :frequency (val %)) freq)))
