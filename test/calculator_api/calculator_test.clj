(ns calculator-api.calculator-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [calculator-api.calculator :refer :all]))


(deftest validator-test
  (is (=
       (valid? " 1 2 3 4 5 ")
       true))
  (is (=
       (valid? "-1 * (2 * 6 / 3)")
       true))
  (is (=
       (valid? "Hello!")
       false)))

#_(validator-test)


(deftest calculate-test
  (is (=
       (:result (calc "4")) 4.0))
  (is (=
       (:result (calc "-73")) -73.0))
  (is (=
       (:result (calc "-0")) 0.0))
  (is (=
       (:error (calc "3.14")) "not valid")) ; Doesn't support floats yet!
  (is (=
       (:result (calc "1+2")) 3.0))
  (is (=
       (:result (calc "123/3")) 41.0))
  (is (=
       (:result (calc "-1 * (2 * 6 / 3)")) -4.0))
  (is (=
       (:result (calc "22/7")) (double (/ 22 7)))) ; NOT equal to Math/PI
  (is (=
       (:result (calc "-44 *33+ (222 - 1) + 5 - 3 /( -12 / -7)")) -1227.75)))

#_(calculate-test)
