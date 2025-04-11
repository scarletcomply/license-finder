(ns scarlet.license-finder.deps.lein
  (:require [babashka.fs :as fs]
            [clojure.tools.deps :as deps]
            [lein2deps.api :as lein-deps]
            [scarlet.license-finder.deps.tools-deps :as tools-deps]))

(defn find-dependencies
  "Collects Clojure dependencies from a lein `project.clj` file."
  [file-path opts]
  (let [path (fs/absolutize file-path)]
    (-> (lein-deps/lein2deps {:project-clj (str path)})
        :deps
        deps/calc-basis
        (tools-deps/basis-deps opts))))
