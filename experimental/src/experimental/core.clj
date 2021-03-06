(ns experimental.core
  (:use [clojure.pprint :only [pprint]])
  (:require #_[clojail.core]
            [clojure.repl]
            [clojure.tools.reader :as r]
            [clojure.tools.reader.reader-types :as rt]))

#_ (defn -main []
  (try (let [sandbox (clojail.core/sandbox #{}) ]
         (sandbox '(do
                     (use 'clojure.repl)
                     (use 'clojure.pprint)
                     (prn (ns-publics 'clojure.repl))
                     (defn f []
                       123)
                     (pprint (source f))
                     (prn (f)))))
    (catch Exception e (clojure.repl/pst e))))

; #=(+ 2 3)

(defn parse-clojure [code]
  (try
    (binding [r/*read-eval* false]
      (->> (str "[" code "]")
        rt/indexing-push-back-reader
        r/read))
    (catch clojure.lang.ExceptionInfo e e)))

(defn -main []
  #_ (pprint (parse-clojure (slurp "src/experimental/core.clj")))
  #_ (prn "--ready")

  (try
    (let [file "(ns aaa
                (:require [clojure.repl]))
                (prn 'ok)
                (def hello \"world\")
                (defn f [x]
                  123)
                (defn ^String f2 [x]
                  123)
                98
                (let [memo 123]
                  (defn g [y]
                    memo))"
          only-declare
          (fn [x]
            (case (first x)
              ns x
              def (with-meta (list 'declare (-> x rest first))
                             (meta x))
              defn x
              nil))]
      (->> file
        parse-clojure
        (filter list?)
        (mapv only-declare)
        (filter identity)
        (map (juxt identity meta))
        clojure.pprint/pprint))
    (catch Exception e (clojure.repl/pst e))))
