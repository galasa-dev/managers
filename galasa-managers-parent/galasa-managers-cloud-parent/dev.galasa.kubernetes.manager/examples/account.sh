#!/bin/sh

kubectl -n galasa1 create serviceaccount galasa

kubectl -n galasa1 get serviceaccounts galasa -o yaml

echo "Look at the secret to obtain the access token"