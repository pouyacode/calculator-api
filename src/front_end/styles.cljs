#_(ns front-end.styles
  (:require-macros
    [garden.def :refer [defcssfn]])
  (:require
    [spade.core   :refer [defglobal defclass]]
    [garden.units :refer [deg px]]
    [garden.color :refer [rgba]]))


#_(defglobal defaults
  [:body
   {:color :#ddd
    :background-color :#222
    :font-size "1.5vh"
    :font-family ["Ubuntu" "Arial" "Helvetica" "sans-serif"]}])

#_(defid outer
  {:margin "20vh auto"
   :width "50vw"
   :padding "1.5vh"
   :background (rgba 136 51 187)})

#_(defclass level1
  []
  {:color :green})

