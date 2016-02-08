(ns clo.core
  (:require [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [clojure.core.async :refer [chan <!!]]
            [uncomplicate.clojurecl
             [core :refer :all]
             [info :refer [info endian-little]]]
            [vertigo
             [bytes :refer [direct-buffer byte-seq]]
             [structs :refer [wrap-byte-seq int8]]]))

(alter-var-root
  (var uncomplicate.clojurecl.core/*opencl-2*)
  (fn [f]  false))

(with-release [dev (first (devices (first (platforms))))])

(let [notifications (chan)
      follow (register notifications)work-sizes (work-size [1])
      work-sizes (work-size [1])
      host-msg (direct-buffer 16)
      program-source
      "__kernel void hello_kernel(__global char16 *msg) {\n    *msg = (char16)('H', 'e', 'l', 'l', 'o', ' ',
   'k', 'e', 'r', 'n', 'e', 'l', '!', '!', '!', '\\0');\n}\n"
   ;(slurp (io/resource "examples/openclinaction/ch04/hello-kernel.cl"))
      ]
  (println "work-sizes: " work-sizes)  
  (println "host-msg: " host-msg)  
  (println "program-source: " program-source)  
  
  (try
    (facts
      "Section 4.1, Page 69."
      (with-release [dev (first (devices (first (platforms))))
                     ctx (context [dev])
                     cqueue (command-queue ctx dev)
                     cl-msg (cl-buffer ctx 16 :write-only)
                     prog (build-program! (program-with-source ctx [program-source]))
                     hello-kernel (kernel prog "hello_kernel")
                     read-complete (event)
                     ]
         (println "dev: " dev)
         (println "ctx: " ctx)
         (println "cqueue: " cqueue)
         (println "cl-msg: " cl-msg)      
         (println "prog: " prog)
         (println "hello-kernel: " hello-kernel)
         (println "read-complete: " read-complete)
         
         (set-args! hello-kernel cl-msg) => hello-kernel
         (enq-nd! cqueue hello-kernel work-sizes) => cqueue
         (enq-read! cqueue cl-msg host-msg read-complete) => cqueue
         (follow read-complete host-msg) => notifications
         ;(apply str (map char
         ;                (wrap-byte-seq int8 (byte-seq (:data (<!! notifications))))))
        ; => "Hello kernel  11111111!!!\0"
      
                  (println "KRAJ " str)

         )
      )
    
    #_(facts
     "Section 4.1, Page 69."
     ;(let [host-msg (direct-buffer 16)
    ;      work-sizes (work-size [1])
    ;      program-source
    ;      (slurp (io/resource "examples/openclinaction/ch04/hello-kernel.cl"))]
      (with-release [dev (first (devices (first (platforms))))
                     ctx (context [dev])
                     cqueue (command-queue ctx dev)
                    cl-msg (cl-buffer ctx 16 :write-only)
                     prog (build-program! (program-with-source ctx [program-source]))
                     hello-kernel (kernel prog "hello_kernel")
                     read-complete (event)]

      (println "dev: " dev)
       (println "ctx: " ctx)
       (println "cqueue: " cqueue)
       (println "cl-msg: " cl-msg)      
       (println "prog: " prog)
       (println "hello-kernel: " hello-kernel)         
         
        
         (set-args! hello-kernel cl-msg) => hello-kernel
         (enq-nd! cqueue hello-kernel work-sizes) => cqueue
         (enq-read! cqueue cl-msg host-msg read-complete) => cqueue
         (follow read-complete host-msg) => notifications
         (apply str (map char
                         (wrap-byte-seq int8 (byte-seq (:data (<!! notifications))))))
         => "Hello kernel  11111111!!!\0"
         
        (println "KRAJ ")    
        )
      ;)
    )    
    
    
    
    
    (catch Exception e (println "Greska: " (.getMessage e)))))







