CREATE TABLE User4DbTest
(
  id       VARCHAR(32)  PRIMARY KEY NOT NULL,
  username VARCHAR(32)                       NOT NULL,
  passwd VARCHAR(32)                       NOT NULL,
  mobile   VARCHAR(32)                       NOT NULL,
  email    VARCHAR(32)                       NOT NULL,
  status   INT DEFAULT 0                     NOT NULL
);
CREATE UNIQUE INDEX "user_username_uindex" ON User4DbTest (username);