(ns calculator-api.database)


(def db (atom {}))


(defn insert [expression result]
  (swap! db assoc expression result))


(defn history []
  @db)
