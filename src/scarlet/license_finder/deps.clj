(ns scarlet.license-finder.deps
  (:require [babashka.fs :as fs]
            [scarlet.license-finder.deps.lein :as lein]
            [scarlet.license-finder.deps.npm :as npm]
            [scarlet.license-finder.deps.shadow-cljs :as shadow-cljs]
            [scarlet.license-finder.deps.tools-deps :as tools-deps]))

(defmulti find-dependencies
  "Finds dependencies for a project file `file-path`.

   Supported `opts`:

   | Option | Description |
   |--------|-------------|
   | `:transitive?` | Whether to consider transitive dependencies |

   Returns a map with the following keys:

   | Key | Description |
   |-----|-------------|
   | `:name` | Name of the dependency, e.g. `\"org.clojure/clojure\"` |
   | `:type` | `:mvn` or `:git` |
   | `:version` | Version string |
   | `:path` | Directory or Jar file where the dependency was found. |
   | `:license` | License information as a map with `:name` and `:url`, or `nil` |"
  (fn [file-path _opts]
    (fs/file-name file-path)))

(defmethod find-dependencies "deps.edn"
  [file-path opts]
  (tools-deps/find-dependencies file-path opts))

(defmethod find-dependencies "shadow-cljs.edn"
  [file-path opts]
  (let [path              (fs/absolutize file-path)
        package-json-path (fs/path (fs/parent path) "package.json")]
    (cond-> (shadow-cljs/find-dependencies file-path opts)
      (fs/exists? package-json-path) (concat (npm/find-dependencies package-json-path opts)))))

(defmethod find-dependencies "package.json"
  [file-path opts]
  (npm/find-dependencies file-path opts))

(defmethod find-dependencies "project.clj"
  [file-path opts]
  (lein/find-dependencies file-path opts))
