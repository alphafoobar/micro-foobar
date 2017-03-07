# How do I merge A into B, without losing history on any side?

[*Answer*](http://stackoverflow.com/a/10548919/7421645) from stack overflow

If you want to merge `project-a` into `project-b`:

<!-- language: lang-sh -->

    cd path/to/project-b
    git remote add project-a path/to/project-a
    git fetch project-a
    git merge --allow-unrelated-histories project-a/master # or whichever branch you want to merge
    git remote remove project-a
    git mv source-dir/ dest/new-source-dir

Taken from: http://stackoverflow.com/questions/2949738/git-merge-different-repositories

This method worked pretty well for me, it's shorter and in my opinion a lot cleaner.

**Note:** The --allow-unrelated-histories parameter only exists since git >= 2.9.

