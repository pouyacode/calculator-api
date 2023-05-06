(ns calculator-api.calculator-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [calculator-api.calculator :refer :all]))

(deftest validator-test
  (is (= (valid? " 1 2 3 4 5 ")
         true))
  (is (= (valid? "-1 * (2 * 6 / 3)")
         true))
  (is (= (valid? "444.4")
         true))
  (is (= (valid? "-512.9")
         true))
  (is (= (valid? "1.111e-11")
         true))
  (is (= (valid? "-1.111E+11")
         true))
  (is (= (valid? "Hello!")
         false)))

#_(validator-test)

(deftest calculate-test
  (is (=
       (:result (calc "4")) "4"))
  (is (=
       (:result (calc "-73")) "-73"))
  (is (=
       (:result (calc "-0")) "0"))
  (is (=
       (:result (calc "3.14")) "3.14"))
  (is (=
       (:result (calc "1+2")) "3"))
  (is (=
       (:result (calc "123/3")) "41"))
  (is (=
       (:result (calc "-1*(2*6/3)")) "-4"))
  (is (not=
       (:result (calc "22/7")) (str Math/PI))) ; NOT equal to Math/PI
  (is (=
       (:result (calc "22/7")) (str (double (/ 22 7)))))
  (is (=
       (:result (calc "-44*33+(222-1)+5-3/(-12/-7)")) "-1227.75"))
  (is (=
       (:result (calc "5+(3*(6+(3-7)))")) "11"))
  (is (=
       (:result (calc "-5+(3*(6+(3-7)))")) "1"))
  (is (=
       (:result (calc "-4-5--2+5+-3*12*-9/2/-6")) "-29"))
  (is (=
       (:result (calc "1.111e-11")) "1.111E-11"))
  (is (=
       (:result (calc "3.14e2+4.5e-3")) "314.0045")))

#_(calculate-test)
