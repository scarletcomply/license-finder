(ns scarlet.license-finder.deps.tools-deps
  (:require [babashka.fs :as fs]
            [clojure.tools.deps :as deps]
            [clojure.tools.deps.extensions :as deps-ext]
            [clojure.tools.deps.util.dir :as deps-dir]))

(defn basis-deps
  "Returns a seq of deps maps from a basis map."
  [{:keys [deps libs] :as basis} {:keys [transitive?]}]
  (->> (if transitive?
         libs
         (map #(vector % (libs %)) (keys deps)))
       (map (fn [[lib coord]]
              {:name    (str lib)
               :type    (:deps/manifest coord)
               :version (or (:mvn/version coord)
                            (:git/tag coord)
                            (:git/sha coord))
               :license (deps-ext/license-info lib coord basis)
               :path    (or (:deps/root coord)
                            (first (:paths coord)))}))))

(defn find-dependencies
  "Returns a seq of deps maps for the `deps.edn` file `file-path`."
  [file-path opts]
  (let [path   (fs/absolutize file-path)
        params (merge {:user nil
                       :project (fs/file-name path)}
                      opts)]
    (basis-deps (deps-dir/with-dir (fs/file (fs/parent path))
                  (deps/create-basis params))
                opts)))
