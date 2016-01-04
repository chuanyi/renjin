# Generated by gen-matrices-sum.R using GNU R version 3.2.0 (2015-04-16)
library(hamcrest)
test.colSums.1 <- function() assertThat(colSums(1:3), throwsError())
test.colSums.2 <- function() assertThat(colSums(structure(1:12, .Dim = 3:4)), identicalTo(c(6, 15, 24, 33), tol = 0.000001))
test.colSums.3 <- function() assertThat(colSums(structure(1:12, .Dim = 3:4, .Dimnames = list(c("a", "b", "c"), c("d", "e", "f", "g")))), identicalTo(structure(c(6, 15, 24, 33), .Names = c("d", "e", "f", "g")), tol = 0.000001))
test.colSums.4 <- function() assertThat(colSums(structure(1:12, .Dim = 3:4, .Dimnames = structure(list(    x = c("a", "b", "c"), y = c("d", "e", "f", "g")), .Names = c("x", "y")))), identicalTo(structure(c(6, 15, 24, 33), .Names = c("d", "e", "f", "g")), tol = 0.000001))
test.colSums.5 <- function() assertThat(colSums(structure(c(1, NA, NA, 3), .Dim = c(2L, 2L))), identicalTo(c(NA_real_, NA_real_), tol = 0.000001))
test.colSums.6 <- function() assertThat(colSums(structure(c(1, NA, NA, 3), .Dim = c(2L, 2L), .Dimnames = list(    c("r1", "r2"), c("c1", "c2")))), identicalTo(structure(c(NA_real_, NA_real_), .Names = c("c1", "c2")), tol = 0.000001))
test.colSums.7 <- function() assertThat(colSums(structure(c(1, NA, NA, 3), .Dim = c(2L, 2L), .Dimnames = structure(list(    x = c("r1", "r2"), y = c("c1", "c2")), .Names = c("x", "y")))), identicalTo(structure(c(NA_real_, NA_real_), .Names = c("c1", "c2")), tol = 0.000001))
test.colSums.8 <- function() assertThat(colSums(structure(1:3, .Dim = c(3L, 1L))), identicalTo(6, tol = 0.000001))
test.colSums.9 <- function() assertThat(colSums(structure(1:3, .Dim = c(3L, 1L), .Dimnames = list(c("a", "b", "c"), NULL))), identicalTo(6, tol = 0.000001))
test.colSums.10 <- function() assertThat(colSums(structure(1:3, .Dim = c(3L, 1L), .Dimnames = structure(list(    x = c("a", "b", "c"), y = NULL), .Names = c("x", "y")))), identicalTo(6, tol = 0.000001))
test.colSums.11 <- function() assertThat(colSums(1:3, na.rm = TRUE), throwsError())
test.colSums.12 <- function() assertThat(colSums(structure(1:12, .Dim = 3:4), na.rm = TRUE), identicalTo(c(6, 15, 24, 33), tol = 0.000001))
test.colSums.13 <- function() assertThat(colSums(structure(1:12, .Dim = 3:4, .Dimnames = list(c("a", "b", "c"), c("d", "e", "f", "g"))), na.rm = TRUE), identicalTo(structure(c(6, 15, 24, 33), .Names = c("d", "e", "f", "g")), tol = 0.000001))
test.colSums.14 <- function() assertThat(colSums(structure(1:12, .Dim = 3:4, .Dimnames = structure(list(    x = c("a", "b", "c"), y = c("d", "e", "f", "g")), .Names = c("x", "y"))), na.rm = TRUE), identicalTo(structure(c(6, 15, 24, 33), .Names = c("d", "e", "f", "g")), tol = 0.000001))
test.colSums.15 <- function() assertThat(colSums(structure(c(1, NA, NA, 3), .Dim = c(2L, 2L)), na.rm = TRUE), identicalTo(c(1, 3), tol = 0.000001))
test.colSums.16 <- function() assertThat(colSums(structure(c(1, NA, NA, 3), .Dim = c(2L, 2L), .Dimnames = list(    c("r1", "r2"), c("c1", "c2"))), na.rm = TRUE), identicalTo(structure(c(1, 3), .Names = c("c1", "c2")), tol = 0.000001))
test.colSums.17 <- function() assertThat(colSums(structure(c(1, NA, NA, 3), .Dim = c(2L, 2L), .Dimnames = structure(list(    x = c("r1", "r2"), y = c("c1", "c2")), .Names = c("x", "y"))), na.rm = TRUE), identicalTo(structure(c(1, 3), .Names = c("c1", "c2")), tol = 0.000001))
test.colSums.18 <- function() assertThat(colSums(structure(1:3, .Dim = c(3L, 1L)), na.rm = TRUE), identicalTo(6, tol = 0.000001))
test.colSums.19 <- function() assertThat(colSums(structure(1:3, .Dim = c(3L, 1L), .Dimnames = list(c("a", "b", "c"), NULL)), na.rm = TRUE), identicalTo(6, tol = 0.000001))
test.colSums.20 <- function() assertThat(colSums(structure(1:3, .Dim = c(3L, 1L), .Dimnames = structure(list(    x = c("a", "b", "c"), y = NULL), .Names = c("x", "y"))),     na.rm = TRUE), identicalTo(6, tol = 0.000001))