;;; This namespace contains code to generate `parser` and through it, parse the
;;; provided math expression. Then we'll turn `infix` notations to `prefix` and
;;; just `eval` the result.
;;; 
;;; Calling `eval` function is pretty safe, since we validate user's input and
;;; also trust `antlr4` to return non-evil result.
(ns calculator-api.calculator
  (:require [clojure.spec.alpha :as spec]
            [clojure.edn :only read-string]
            [clojure.core :only eval])
  (:import [org.antlr.v4.runtime ANTLRInputStream CommonTokenStream]
           [java.io FileInputStream]
           [grammar.expr ExprLexer ExprParser]))


;;; We use an atom to keep track of evaluated expressions. It could've been some
;;; SQL Database, but IMHO it'd add complexity to this project. My focus is to
;;; parse mathematical expressions.
;;; There's no specific function to work with this atom, we just `swap!` it once
;;; in `calc` function and `deref` once in `calculator-api.service/hist`
;;; function.
(def history (atom {}))


;;; A rather strict `spec` validator that only allows `digits`, `parenthesis`,
;;; basic mathematical operations and of course "white-space".
;;; 
;;; It doesn't allow expressions that start with `*`, `/`, `+`.
(spec/def ::valid-expression #(re-matches #"(?=[^+*/])[\+\-\*\/\d\(\)\ ]+" %))

(defn valid?
  "Simple regex validator that only lets digits and basic operations pass.
  Also checks for expression length, just 50 or fewer characters can pass."
  [expression]
  (and (> 51 (count expression))
       (spec/valid? ::valid-expression expression)))


(defn parser
  "Helper function to create `antlr4` parser.
  Nothing special. Just some java interop based on `antlr4` documents.
  This function should remain as-is. It's the only way to create parser, unless
  you want to use some Clojure wrapper instead of calling java directly.

  An alternative to this method would be https://github.com/aphyr/clj-antlr.
  I tried it, and for our purpose, its output is a bit difficult to process.

  `AntlrInputStream` and `CommonTokenStream` are part of `antlr4` that you must
  install. Instructions are written in README.md file.

  `ExprLexer` and `ExprParser` are generated from math grammar I wrote in
  `Expr.g4` file. Refer to README.md for more information."
  [exp]
  (-> exp
      ANTLRInputStream.
      ExprLexer.
      CommonTokenStream.
      ExprParser.))


;;; We call this function before we declare it.
;;;
;;; `clean-arg`, `apply-arg` and `inline` are interconnected tightly.
;;; Read their documents in order and pay attention to their `argument` names.
;;;
;;; My apologies if their comments are a bit difficult to grasp. I tried my
;;; best to explain their work. However, since their argument names are
;;; consistent, you might find their code much easier to understand :)
;;;
;;; > "Code never lies, comments sometimes do." - Ron Jeffries
(declare inline)


(defn clean-arg
  "If provided with a list, it'd call `inline` function, otherwise
  (if it was a digit or a symbol in our case) it'd just return the input.

  We use it when walking through our `ast` and return the `leafs` as-is, but
  modify the `branches` to turn them into a proper syntax for our Clojure
  program to evaluate."
  [arg]
  (if (seq? arg)
    (inline arg)
    arg))


(defn apply-arg
  "Turn `(x (+ y)), into `(+ a b)` with some help from `clean-arg` function.

  `apply-arg` and `clean-arg` functions go hand-in-hand to help flipping nodes
  and branches of our tree to turn `infix` to `prefix` notation.

  Note that first argument (`arg1`) will be untouched. Read comments for
  `inline` function for more information."
  [arg1 [op arg]]
  (list op arg1 (clean-arg arg)))


(defn inline
  "The main part of our tree parser! Calls `clean-arg` and `apply-arg` on every
  node of our tree.

  These three functions could be composed into one, but would make ugly and
  non-Clojurish look. Together they make some sort of recursion that walks
  our tree in a breadth-first fashion.

  When you give it a `list` it extracts the first element of that list and
  assigns it to `arg1`, after parsing it using `clean-arg` function, sends
  it to `apply-arg` with the partitioned version of \"the rest of the list\"
  (`ops-and-args`).

  If the provided input is just a negative value `(- 1234)`, return it without
  passing through `apply-arg`. Because `apply-arg` looks for a `seq` of three."
  [[arg1 & ops-and-args]]
  (if (= 1 (count ops-and-args))        ; Check for something like `(- 5)`
    (list arg1 (clean-arg ops-and-args)) ; And return it as-is
    (let [ops (partition 2 ops-and-args)]
      (reduce apply-arg (clean-arg arg1) ops))))


(defn prepare
  "After validating input, create the Antlr4 `parser` and parse it to make a
  `tree`. Read the string output of Antlr4, get rid of unwanted pieces of
  information and return the result. Result will be a list (`seq`).
  
  Returns `nil` if input is not valid."
  [expression]
  (if (valid? expression)
    (let [pars (parser expression)
          tree (.prog pars)]
      (-> tree
          (.toStringTree pars)
          read-string
          (clojure.string/replace #"prog|expr" "")
          read-string))
    nil))


(defn ratio
  "If it's a ratio, convert to `double`, then convert to `str` for JSON.
  Otherwise just convert it to `str`"
  [num]
  (if (ratio? num)
    (str (double num))
    (str num)))


(defn calc
  "If `prepare` returns a value (other than `nil`) pass it through `inline`
  function to traverse the tree and get a prefixed version of parsed data.
  The result would be a beautiful `ast`, ready for Clojure's `eval` function.

  At this stage, it's safe to use `eval` function. Since user's input has passed
  a long journey through our `valid?` and Antlr`s grammar and parser. We are
  absolutely sure we only pass digits and basic arithmetic operations."
  [expression]
  (if-let [result (-> expression
                      prepare
                      inline
                      eval
                      (#(if % (ratio %))))]
    (do (swap! history conj {expression  result})
        {:result result})
    {:error "not valid"}))


(defn dbug
  "A less advanced version of `calc` function. It doesn't `eval` anything, just
  returns the parsed data and its in-lined version."
  [expression]
  (if-let [parsed (prepare expression)]
    {:parsed (str parsed)
     :sexp (str (inline parsed))}
    {:error "not valid"}))
