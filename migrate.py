#! /usr/bin/env python

import os.path
import subprocess
import shutil

WIDGETS_REPO_PATH = '.'
WEBSITE_REPO_PATH = '../website/'

TAG_SCRIPT = './getCommitTag.pl'

WIDGET_DIRECTORIES = {'war/': 'grapher2/',}
OUTPUT_SUBDIR = 'public/'
OUTPUT_DIRECTORY = WEBSITE_REPO_PATH + OUTPUT_SUBDIR

def copyWidgets():
    '''
    Copies over widgets from WIDGETS_REPO_PATH to WEBSITE_REPO_PATH
    '''
    shouldContinue = checkGit(WIDGETS_REPO_PATH) \
        and checkGit(WEBSITE_REPO_PATH)

    commitTag = getCommitTag(WIDGETS_REPO_PATH)

    if not shouldContinue:
        print 'Please commit both your git repositories!'
        return
    
    # Copy over widget directories
    for key, value in WIDGET_DIRECTORIES.iteritems():
        src = key + value
        dest = OUTPUT_DIRECTORY + value

        # Remove any old copy of dest
        if os.path.exists(dest):
            shutil.rmtree(dest)

        # 'git rm' the old version
        olddir = pCall('pwd')
        os.chdir(OUTPUT_DIRECTORY)
        pCall('git rm -rf %s' % value)
        os.chdir(olddir)

        # Now copy over current version
        shutil.copytree(src, dest)

    # Make a commit on the website repo
    os.chdir(OUTPUT_DIRECTORY)

    for value in WIDGET_DIRECTORIES.itervalues():
        pCall('git add %s' % value)

    pCall(r'git commit -m "Automatic grapher update from commit %s"'
        % commitTag)

def checkGit(path):
    '''
    Also handles the user interaction to check that a repo is committed
    '''
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
    '''
    Checks that git has no changed files since the last commit
    '''
    gitProcess = subprocess.Popen('git status'.split(),
                                  stdout = subprocess.PIPE,
                                  cwd = path)
    output = gitProcess.communicate()

    linesList = output[0].split('\n')

    for line in linesList:
        if 'Changes to be committed' in line:
            return False

        if 'nothing to commit' in line:
            return True

    return linesList

def getCommitTag(path):
    '''
    Returns the hash of the current commit in the repo at path
    '''
    tagProcess = subprocess.Popen(TAG_SCRIPT.split(),
                                  stdout = subprocess.PIPE,
                                  cwd = path)
    output = tagProcess.stdout
    
    tag = output.readline()

    output.close()

    return tag

def pCall(cmd):
    '''
    Calls cmd and returns the standard output from cmd
    '''
    print cmd
    process = subprocess.Popen(cmd, shell = True)

    output = process.communicate()

    if output[0]:
        print '%s\n' % output[0]

    return output[0]

if __name__ == '__main__':
    copyWidgets()

