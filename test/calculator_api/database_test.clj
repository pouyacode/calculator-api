(ns calculator-api.database-test
  (:require [clojure.test :refer :all]
            [calculator-api.database :refer :all]))

(deftest database-test
  (is (= (sequential? (history))
         true))
  (is (= (map? (first (history)))
         true)))

#_(database-test)
