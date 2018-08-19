# CS725 Assignment 1

- All commands completed. 
- Currently runs on `localhost`; port `6789`.

## Run instructions

lskfdlskfksdk

## Logging in
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
account: sere_acc
password: sere_pw
```

- User ID is only used for checking whether or not the user ID exists in the system.
    - Unless user ID is `admin`, in which case it will log you straight in to the system, bypassing `account` or `password` checks.
- To log in to the system, `account` and `password` is required. 

## Directory structure 
TODO

## Commands
### `USER` `<user-id>`

Checks whether or not `user-id` exists in the system.

Example:
```
# user a
+User-id valid, send account and password
```

### `ACCT` `<account>`

The account to log in as.

Example:
```
$ ACCT a
+Account valid, send password
```
```
$ ACCT does_not_exist
-Account does not exist
```
TODO
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