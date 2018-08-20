# CS725 Assignment 1

- All commands completed.
- Tested on Windows on Eclipse.

## Run instructions

1. Open Eclipse
2. Open project from file system
3. Expand `src` directory in the Package Explorer.
4. Run `TCPServer.java` found under `src/server` folder.
5. Run `TCPClient.java` found under the `src/client` folder.
6. `Create new console view` to view consoles for both programs.
7. Select `TCPServer.java` tab and select `Display selected console` to view both consoles.
8. Input should be entered in the `TCPClient` console.

## Notes

- Whenever a `-` response is received from the server, the connection will be aborted.  Both programs must be executed again.
- Runs on `localhost`; port `6789`.
- Examples show user input prefixed with `$`.  This is for display purposes and should not be included in your input.
- 'Current directory' refers to the currently-assigned directory.  Before a `CDIR` command is run, this refers to `res/server/` on the server side, and `res/client/` on the client side.
- When a directory/file is specified, it should be relative to the 'current directory'.

### Logging in
There are currently 3 accounts for testing, stored in `res/users.json`:

```
user id: a
account: a
password: a
```
```
user id: b
account: bb
password: bbb
```
```
user id: serena
account: account
password: pw
```

- User ID is only used for checking whether or not the user ID exists in the system.
    - Unless user ID is `admin`, in which case it will log you straight in to the system, bypassing `account` or `password` checks.
- To log in to the system, `account` and `password` is required. 

### Directory structure 

Assuming root folder is `cs725_a1`,
- Source files are located in `/src/`
- Resources are located in `/res/`
	- Files stored on the server are located in `/res/server/`
	- Files stored on the client side are located in `/res/client/`

## Commands
### `USER` `<user-id>`

Checks whether or not `user-id` exists in the system.

Example of valid user ID:
```
$ USER a
+User-id valid, send account and password
```

Example of invalid user ID:
```
$ USER c
-Invalid user-id, try again
```

Example of ADMIN user ID:
```
$ USER admin
!ADMIN logged in
```


### `ACCT` `<account>`

The account to log in as.

Example of valid account:
```
$ ACCT a
+Account valid, send password
```

Example of invalid account:
```
$ ACCT does_not_exist
-Invalid account, try again
```

Example of ADMIN account:
```
$ ACCT ADMIN
! Account valid, logged-in
```


### `PASS` `<password>`

The password for the account you are trying to log in with.

Example of correct password:
```
$ PASS a
! Logged in
```

Example of incorrect password:
```
$ PASS B
-Wrong password, try again
```

### `TYPE` `{A | B | C }`

Changes the mapping of the stored file and transmission byte stream.  Default mode is binary. 

Example of valid type:
```
$ TYPE C
+Using Continuous mode
```

Example that this command cannot be specified until a `!` response is received from the remote system:
```
$ TYPE A
-Not logged in
```

Example of invalid type:
```
$ TYPE F
-Type not valid
```

### `LIST` `{F | V}` `<directory>`

List all files and folders under `directory`.

If `directory` is not specified, the current directory will be used.

`F` specifies the standard-formatted directory listing; only showing filenames.

`V` specifies the verbose directory listing.  This displays filename, last modified date and time, filesize, and file owner.

Example of `LIST F`:
- Navigate to `/res/server` and observe the files in the directory.
```
$ LIST F
+E:\Documents\cs725_a1\res\server\
client.txt
retr_test.txt
```
The output should match the files shown.

Example of `LIST V`:
```
$ LIST V
+E:\Documents\cs725_a1\res\server\
client.txt    20/08/2018 20:34    1    BUILTIN\Administrators
retr_test.txt    20/08/2018 20:20    8    BUILTIN\Administrators
```

Example of `LIST F`, specifying directory `test`:
```
$ LIST F test
+E:\Documents\cs725_a1\res\server\test
a.txt
sdfsdf.txt
```

Example of invalid `directory`:
```
$ LIST F invalid
-Could not get directory listing because directory does not exist
```

### `CDIR` `new-directory`

Changes current working directory/"current directory" to `new-directory` if already logged in.
If not logged in, this command will be 'queued' -- the directory will be changed once logged in.

Example of not logged in:
```
$ CDIR test
+directory ok, send account/password
ACCT admin
!Changed working dir to test
```

Example of logged in:
```
$ CDIR test2
!Changed working dir to test2
```

Example of non-existent directory:
```
$ CDIR a
-Can't connect to working directory because: Directory does not exist
```

### `KILL` `file-spec`

Deletes `file-spec` from the current working directory on the server side.

Example of successful deletion:
Confirm the existence of `a.txt` in current working directory.
```
$ KILL a.txt
+a.txt deleted
```
File can now be observed to be deleted from directory.

Example of unsuccessful deletion -- file does not exist:
```
$ KILL b
-File could not be deleted
```

### `NAME` `old-file-spec`

Specifies the file to be renamed.  `TOBE` `new-file-spec` should be sent if a `+` response is received.

Example of usage on existing file:
Confirm the existence of test.txt in the current working directory.
```
$ NAME test.TXT
+FILE EXISTS
$ TOBE renamed.txt
+test.txt renamed to renamed.txt
```
Observe that `test.txt` no longer exists, and has been renamed to `renamed.txt`.

Example of usage on non-existent file:
```
$ NAME does_not_exist.txt
-Can't find does_not_exist.txt
```

### `DONE`

Tells remote system you are done.  Closes connection on both sides.

Example:
```
$ DONE
+localhost closing connection
```
