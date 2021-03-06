(ns com.puppetlabs.test.http
  (:use [com.puppetlabs.http]
        [clojure.test]))

(deftest conneg
  (testing "content negotiation"
    (testing "should match an exact accept header"
      (is (= "text/html" (acceptable-content-type "text/html" "text/html"))))

    (testing "should match an exact accept header that includes other types"
      (is (= "text/html" (acceptable-content-type "text/html" "text/html, text/plain"))))

    (testing "should match a wildcard accept header"
      (is (= "text/*" (acceptable-content-type "text/html" "text/*"))))

    (testing "should match a wildcard accept header that includes other types"
      (is (= "text/*" (acceptable-content-type "text/html" "text/plain, text/*"))))

    (testing "should return nil if a single header doesn't match"
      (is (= nil (acceptable-content-type "text/html" "application/json"))))

    (testing "should return nil if no headers match"
      (is (= nil (acceptable-content-type "text/html" "text/plain, application/json")))
      (is (= nil (acceptable-content-type "text/html" "text/plain, application/*"))))))

(deftest uri-to-segments
  (testing "splitting a url into segments"
    (testing "should work for partial urls"
      (is (= ["foo" "bar"] (uri-segments "foo/bar"))))

    (testing "should work for empty urls"
      (is (= [] (uri-segments ""))))

    (testing "should work for common urls"
      (is (= ["foo" "bar" "baz"] (uri-segments "/foo/bar/baz"))))

    (testing "should remove empty segments"
      (is (= ["foo" "bar"] (uri-segments "/foo//bar"))))))

(deftest content-type-checking
  (let [test-app (must-accept-type (constantly 10) "foo")]
    (testing "ensuring a given content-type is accepted"
      (testing "should call the wrapped function if the right content-type is accepted"
        (let [headers {"accept" "foo"}
              request {:headers headers}
              response (test-app request)]
          (is (= response 10))))
      (testing "should respond with a failure if the content-type is not accepted"
        (let [headers {"accept" "bar"}
              request {:headers headers}
              response (test-app request)]
          (is (= (:status response) 406))
          (is (= (:body response) "must accept foo")))))))
