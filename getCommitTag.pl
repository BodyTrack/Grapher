#!/usr/bin/perl
use warnings;
use strict;

my $git_branch = `git branch --no-color 2>/dev/null`;
($git_branch) = ($git_branch =~ /\* (\S+)/s);
my $git_commit = `git show-ref --abbrev -s --verify refs/heads/$git_branch
2>/dev/null`;
chomp $git_commit;
system('git status -s >/dev/null');
my $git_modified = '+';
$git_modified = '' if ($?);
my $git_version = "$git_branch:$git_commit$git_modified";

print $git_version;
