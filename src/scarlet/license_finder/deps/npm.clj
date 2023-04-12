(ns scarlet.license-finder.deps.npm
  (:require [babashka.fs :as fs]
            [clojure.data.json :as json]))

(defn- read-json [file]
  (-> file fs/file slurp json/read-str))

;; TODO: "(ISC OR GPL-3.0)" -> multiple licenses
;; TODO: "SEE LICENSE IN <filename>"
(defn- package-json-license [file]
  (when-let [n (get (read-json file) "license")]
    {:id n}))

(defn- transitive-closure [deps lock-map]
  (into #{} (mapcat (fn [dep-name]
                      (into #{dep-name}
                            (some-> (get-in lock-map ["dependencies" dep-name "requires"])
                                    keys (transitive-closure lock-map)))))
        deps))

(defn find-dependencies
  "Collects NPM dependencies from a `package.json` file and its accompanying
   `package-lock.json`.  Does not work with Yarn lock files."
  [file-path {:keys [transitive?]}]
  (let [path        (fs/absolutize file-path)
        project-dir (fs/parent path)
        package-map (read-json path)
        lock-map    (read-json (fs/file project-dir "package-lock.json"))
        deps        (keys (get package-map "dependencies"))]
    (->> (if transitive?
           (transitive-closure deps lock-map)
           deps)
         (map (fn [dep-name]
                (let [resolved (get-in lock-map ["dependencies" dep-name])
                      dist-dir (fs/path project-dir "node_modules" dep-name)]
                  {:type    :npm
                   :name    dep-name
                   :version (get resolved "version")
                   :license (package-json-license (fs/file dist-dir "package.json"))
                   :path    (str dist-dir)}))))))
