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

Example:
```
$ PASS a
! Logged in
```
TODO

### `TYPE` `{A | B | C }`

Changes the mapping of the stored file and transmission byte stream.  Default mode is binary. TODO

Example:
```
$ TYPE C
+Using Continuous mode
```
TODO

### `LIST` `{F | V}` `<directory>`

List all files and folders under `directory`.

If `directory` is not specified, the current directory will be used.  

`F` specifies the standard-formatted directory listing; only showing filenames.

`V` specifies the verbose directory listing.  This displays filename, last modified date and time, filesize, and file owner.