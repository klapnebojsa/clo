(ns clo.core
  (:require [uncomplicate.clojurecl
             [core :refer :all]
             [info :refer [info endian-little]]]
            [vertigo
             [bytes :refer [direct-buffer byte-seq]]
             [structs :refer [wrap-byte-seq int8]]]))

(alter-var-root
  (var uncomplicate.clojurecl.core/*opencl-2*)
  (fn [f]  false))

(with-release [dev (first (devices (first (platforms))))])

;(def dev (first (devices (first (platforms)))))
(let [work-sizes (work-size [1])
      host-msg (direct-buffer 16)
      program-source
      "__kernel void hello_kernel(__global char16 *msg) {\n    *msg = (char16)('H', 'e', 'l', 'l', 'o', ' ',
   'k', 'e', 'r', 'n', 'e', 'l', '!', '!', '!', '\\0');\n}\n"
      ]
  (println "work-sizes: " work-sizes)  
  (println "host-msg: " host-msg)  
  (println "program-source: " program-source)  
  
  (try
    (with-release [dev (first (devices (first (platforms))))
                     ctx (context [dev])
                     cqueue (command-queue ctx dev)
                     cl-msg (cl-buffer ctx 16 :write-only)
                     prog (build-program! (program-with-source ctx [program-source]))
                     hello-kernel (kernel prog "hello_kernel")]
      (println "dev: " dev)
      (println "ctx: " ctx)
      (println "cqueue: " cqueue) 
      (println "prog: " prog)
      (println "hello-kernel: " hello-kernel)
      
      (set-args! hello-kernel cl-msg)
      (enq-nd! cqueue  hello-kernel work-sizes)
      (enq-read! cqueue cl-msg host-msg)
      (apply str (map char
                      (wrap-byte-seq int8 (byte-seq host-msg)))))
    (catch Exception e (println "Greska: " (.getMessage e)))))







