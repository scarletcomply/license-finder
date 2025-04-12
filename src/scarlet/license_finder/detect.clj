(ns scarlet.license-finder.detect
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [scarlet.license-finder.licenses :as licenses])
  (:import [java.nio.file
            FileSystem
            FileSystems
            Path
            PathMatcher]))

(set! *warn-on-reflection* true)

(def license-file-patterns
  "Patterns for known paths of License files within a repository."
  ["LICENSE" "LICENSE.{txt,md,html}" "license/LICENSE"])

(def jar-license-file-patterns
  (into [] cat [(map (partial str "META-INF/") license-file-patterns)
                (map (partial str "META-INF/**/") license-file-patterns)
                license-file-patterns]))

(defn- find-files
  "Recursively look for files in `dir-path` that match one of the
   glob `patterns`."
  [^FileSystem fs dir-path patterns]
  (let [matchers (map (fn [^String pattern]
                        ;; The path that will be matched against the pattern contains a full canonicalPath.
                        ;; The patterns must therefore cater for the dir-path prefix, when setting up the glob
                        (.getPathMatcher fs (str "glob:" dir-path java.io.File/separator pattern)))
                      patterns)
        matches? (fn [^Path path]
                   (some (fn [^PathMatcher m]
                           (.matches m path))
                         matchers))
        found    (atom (transient []))]
    (fs/walk-file-tree dir-path
                       {:visit-file (fn [path _attrs]
                                      (when (matches? path)
                                        (swap! found conj! path))
                                      :continue)})
    (persistent! @found)))

(defn- detect-license-file
  "Attempts to detect a known license from its license file."
  [uri]
  (let [text (slurp uri)]
    (some (fn [[search id]]
            (when (str/includes? text search)
              {:id id
               :name search}))
          (licenses/common-license-names))))

(defn- find-license-file
  "Searches for a license file and attempts to detect the license name."
  [fs dir-path patterns]
  (->> (find-files fs dir-path patterns)
       (keep (fn [^Path p]
               (let [uri (.toUri p)]
                 (when-let [license (detect-license-file uri)]
                   (assoc license :file-uri (str uri))))))
       first))

(defn- detect-license-in-dir [dir-path]
  (find-license-file (FileSystems/getDefault) dir-path license-file-patterns))

(defn- detect-license-in-jar [file-path]
  (let [^Path        path   (fs/path file-path)
        ^ClassLoader loader nil]
    (with-open [fs (FileSystems/newFileSystem path loader)]
      (find-license-file fs
                         (.getPath fs "" (make-array String 0))
                         jar-license-file-patterns))))

(defn detect-license
  "Find a license file in a dependency path, which is assumed to be a
   directory or Jar file.  Returns a map with keys `:name` and `:url`
   when found, same as tools.deps."
  [path]
  (cond
    (str/ends-with? (fs/file-name path) ".jar") (detect-license-in-jar path)
    (fs/directory? path) (detect-license-in-dir path)
    :else nil))
