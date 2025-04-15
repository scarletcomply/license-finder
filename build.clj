(ns build
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(defn- git [& args]
  (let [{:keys [exit out]}
        (b/process {:command-args (into ["git"] args)
                    :dir "."
                    :out :capture
                    :err :ignore})]
    (when (and (zero? exit) out)
      (str/trim-newline out))))

(def lib 'com.scarletcomply/license-finder)

(def repo-url (str "https://github.com/scarletcomply/license-finder"))

(def tagged  (git "describe" "--tags" "--exact-match"))
(def version (str/replace (or tagged
                              (git "describe" "--tags")
                              (git "rev-parse" "--short" "HEAD"))
                          #"^v" ""))

(def scm {:connection (str "scm:git:" repo-url)
          :tag        (or tagged "HEAD")
          :url        repo-url})

(def pom-data
  [[:licenses
    [:license
     [:name "MIT License"]
     [:url "https://opensource.org/license/mit/"]]]])

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean "Clean the target directory." [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (println jar-file)
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]
                :scm scm
                :pom-data pom-data})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn deploy [_]
  (dd/deploy {:installer :remote
              :artifact  (b/resolve-path jar-file)
              :pom-file  (b/pom-path {:lib lib
                                      :class-dir class-dir})}))
