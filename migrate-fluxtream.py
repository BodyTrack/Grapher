#!/usr/bin/env python

import os
import os.path
import subprocess
import shutil

WIDGETS_REPO_PATH = '.'
WEBSITE_REPO_PATH = '../fluxtream-app/'

TAG_SCRIPT = './getCommitTag.pl'

INPUT_DIR = 'war/grapher2/'
OUTPUT_DIR = 'fluxtream-web/src/main/webapp/static/grapher4/'

def copyWidgets():
    'Copies over widgets from WIDGETS_REPO_PATH to WEBSITE_REPO_PATH'
    shouldContinue = checkGit(WIDGETS_REPO_PATH) \
        and checkGit(WEBSITE_REPO_PATH)

    commitTag = getCommitTag(WIDGETS_REPO_PATH)

    if not shouldContinue:
        print 'Please commit both your git repositories!'
        return

    # 'git rm' the old version
    olddir = os.path.abspath(os.path.curdir)
    os.chdir(WEBSITE_REPO_PATH)
    pCall('git rm -rf %s' % OUTPUT_DIR)
    os.chdir(olddir)

    # Now copy over current version
    shutil.copytree(INPUT_DIR, WEBSITE_REPO_PATH + OUTPUT_DIR)

    # Make a commit on the website repo
    os.chdir(WEBSITE_REPO_PATH)
    pCall('git add %s' % OUTPUT_DIR)
    pCall(r'git commit -m "Automatic grapher update from commit %s"'
        % commitTag)

def checkGit(path):
    'Also handles the user interaction to check that a repo is committed'
    shouldContinue = True

    committed = gitIsCommitted(path)
    if committed == False:
        print 'Please make all commits to git in %s' \
            % os.path.abspath(path)
        shouldContinue = False
    elif committed != True:
        print 'git output in repository %s:' % path
        print 'If we attempt to copy widgets without a fully committed ' \
              'repository, there is a chance of data loss'
        print '\n'.join([line for line in committed])
        shouldContinue = raw_input(
            'Do you want to continue (y/n)? ').lower() == 'y'

    return shouldContinue

def gitIsCommitted(path):
    'Checks that git has no changed files since the last commit'
    gitProcess = subprocess.Popen('git status'.split(),
                                  stdout=subprocess.PIPE,
                                  cwd=path)
    stdout, _ = gitProcess.communicate()
    lines = stdout.split('\n')

    for line in lines:
        if 'Changes to be committed' in line:
            return False
        if 'nothing to commit' in line:
            return True

    return lines

def getCommitTag(path):
    'Returns the hash of the current commit in the repo at path'
    tagProcess = subprocess.Popen(TAG_SCRIPT.split(),
                                  stdout=subprocess.PIPE,
                                  cwd=path)
    stdout, _ = tagProcess.communicate()
    return stdout.strip()

def pCall(cmd, print_output=True):
    'Calls cmd and returns the standard output from cmd'
    print '%s' % cmd
    process = subprocess.Popen(cmd, shell=True)

    stdout, _ = process.communicate()
    if print_output and stdout:
        print '%s\n' % stdout
    return stdout

if __name__ == '__main__':
    copyWidgets()

