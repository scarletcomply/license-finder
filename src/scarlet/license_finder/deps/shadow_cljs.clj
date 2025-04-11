(ns scarlet.license-finder.deps.shadow-cljs
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.tools.deps :as deps]
            [clojure.tools.deps.util.dir :as deps-dir]
            [scarlet.license-finder.deps.tools-deps :as tools-deps]))

(defn- read-edn [file]
  (-> file fs/file slurp edn/read-string))

(defn- lein-deps->deps-edn-deps
  [deps]
  (into {} (for [[dep version] deps]
             [(if (namespace dep)
                dep
                (let [n (name dep)]
                  (symbol n n)))
              {:mvn/version version}])))

(defn- shadow-edn-map->deps-edn-map [shadow-map]
  (when (seq (:dependencies shadow-map))
    {:deps (lein-deps->deps-edn-deps (:dependencies shadow-map))}))

(defn- shadow-basis-params
  "Returns a params map for `create-basis` from a parsed shadow-cljs.edn map."
  [shadow-map]
  (let [params (if-let [deps (:deps shadow-map)]
                 (if (and (map? deps) (:aliases deps))
                   (select-keys deps [:aliases])
                   {})
                 {:project nil})]
    (if-let [extra (shadow-edn-map->deps-edn-map shadow-map)]
      (assoc params :extra extra)
      params)))

(defn find-dependencies
  "Collects Clojure dependencies from a `shadow-cljs.edn` file."
  [file-path opts]
  (let [path       (fs/absolutize file-path)
        shadow-map (read-edn path)]
    (when-let [params (shadow-basis-params shadow-map)]
      (tools-deps/basis-deps (deps-dir/with-dir (fs/file (fs/parent path))
                    (deps/create-basis params))
                  opts))))
