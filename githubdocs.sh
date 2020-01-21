#!/bin/sh +e

echo "Checking for changes"
CHANGED=$(git status -s .)
echo $CHANGED

if [ -z "$CHANGED" ]; then
    echo "No changes detected"
    exit 0
fi

echo "Configing user"
git config user.name '$DOC_USER'
git config user.email $DOC_EMAIL

echo "Adding changes"
git add .

echo "Committing changes"
git commit -m "Generated manager docs $BUILD_TAG"

echo "Pushing branch"
git push -f origin $DOC_BRANCH


echo "Checking for Pull Request"
PR=$(hub pr list -h $DOC_BRANCH)
echo $PR
if [ ! -z "$PR" ]; then
   echo 'Pull Request is open'
  exit 0
fi

echo "No Pull Request exists, creating one"

hub pull-request -b next -h $DOC_BRANCH -m "Autogenned Manager Documentation" -m "Jenkins run $BUILD_TAG" -r $DOC_REVIEWERS