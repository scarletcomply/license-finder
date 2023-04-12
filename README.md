# license-finder

[![Build Status](https://img.shields.io/github/actions/workflow/status/scarletcomply/license-finder/ci.yml?branch=main)](https://github.com/scarletcomply/license-finder/actions)
[![cljdoc](https://cljdoc.org/badge/com.scarletcomply/license-finder)][cljdoc]
[![Clojars](https://img.shields.io/clojars/v/com.scarletcomply/license-finder.svg)][clojars]

## Overview

`license-finder` is a Clojure library that finds the licenses of the dependencies used in your Clojure(Script) projects. The library supports `deps.edn`, `shadow-cljs.edn`, and `package.json` (with npm's `package-lock.json`). With it, you can easily generate a report of the licenses used by your project's dependencies, making it easier to comply with open source licenses and legal requirements.


## Use Cases

`license-finder` can be useful in a variety of situations. For example:

- If you are building a project for a client or an employer, you may need to provide a report of the licenses used by the project's dependencies to ensure compliance with open source licenses and legal requirements.
- If you are working on an open source project, you may want to provide a report of the licenses used by the project's dependencies to help potential contributors understand the project's licensing requirements.
- If you are a developer who is concerned about the licenses of the dependencies used by your projects, license-finder can help you quickly and easily generate a report of the licenses used by those dependencies.

## Usage

To use `license-finder`, require the `find-licenses` function from the `scarlet.license-finder.core` namespace:

```clojure
(require '[scarlet.license-finder.core :refer [find-licenses]])
```

You can then use `find-licenses` to get license information of dependencies in a project file:

```clojure
(find-licenses "deps.edn")
;; => ({:name "babashka/fs"
;;      :type :mvn
;;      :version "0.3.17"
;;      :license {:id "EPL-1.0"
;;                :name "Eclipse Public License 1.0"
;;                :url "http://opensource.org/licenses/eclipse-1.0.php"}
;;      :path "/home/user/.m2/repository/babashka/fs/0.3.17/fs-0.3.17.jar"}
;;     {:name "io.github.clojure/tools.build"
;;      :type :mvn
;;      :version "0.9.4"
;;      :license {:id "EPL-1.0"
;;                :name "Eclipse Public License 1.0"
;;                :url "https://opensource.org/licenses/eclipse-1.0.php"}
;;      :path "/home/user/.m2/repository/io/github/clojure/tools.build/0.9.4/tools.build-0.9.4.jar"}
;;     ,,,)
```

By default, `find-licenses` only considers the direct dependencies of the project. If you want to include transitive dependencies, pass `:transitive? true` as an option:


```clojure
(find-licenses "deps.edn" :transitive? true)
```


### Generating a License Report

You can create a CSV report of your licenses, e.g. in Continuous Integration,
with `scarlet.license-finder.report/write-csv`.


Here is an example on how to integrate `license-finder` in `tools.build`:

```clojure
(ns build
  (:require [scarlet.license-finder.core :as license-finder]
            [scarlet.license-finder.report :as report]))

(defn licenses [{:keys [project]
                 :or   {project "./deps.edn"}}]
  (->> (license-finder/find-licenses project :transitive? true)
       (report/write-report {:project project})))
```

You can then generate a license report via the command line:

```
> clojure -T:build licenses
Licenses written to target/licenses/deps.edn.csv
```

## Installation

Releases are available from [Clojars][clojars].

deps.edn:

```clojure
com.scarletcomply/license-finder {:mvn/version "0.1.5"}
```

Leiningen/Boot:

```clojure
[com.scarletcomply/license-finder "0.1.5"]
```

## License

Copyright 2023 [Scarlet]  
Distributed under the [MIT License].

[MIT License]: ./LICENSE
[Scarlet]: https://scarletcomply.com

[cljdoc]: https://cljdoc.org/jump/release/com.scarletcomply/license-finder
[clojars]: https://clojars.org/com.scarletcomply/license-finder
