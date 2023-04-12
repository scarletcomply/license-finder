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

(defn- git-tag []
  (git "describe" "--tags" "--exact-match"))

(def lib 'com.scarletcomply/license-finder)
(def base-version "0.1")

(def repo-url (str "https://github.com/scarletcomply/license-finder"))

(def tagged  (git-tag))
(def version (if tagged
               (str/replace tagged #"^v" "")
               (format "%s.%s-%s" base-version (b/git-count-revs nil)
                       (if (System/getenv "CI") "ci" "dev"))))

(def scm {:connection (str "scm:git:" repo-url)
          :tag        (or tagged "HEAD")
          :url        repo-url})

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn- next-tag []
  (format "v%s.%s" base-version (b/git-count-revs nil)))

(defn info [_]
  (println "Version: " version)
  (println "Next tag:" (next-tag)))

(defn tag [_]
  (let [tag (format "v%s.%s" base-version (b/git-count-revs nil))]
    (git "tag" "-m" (str "Release " tag) tag)
    (println "Tagged" tag)))

(defn clean "Clean the target directory." [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]
                :scm scm})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn deploy [_]
  (dd/deploy {:installer :remote
              :artifact  (b/resolve-path jar-file)
              :pom-file  (b/pom-path {:lib lib
                                      :class-dir class-dir})}))
