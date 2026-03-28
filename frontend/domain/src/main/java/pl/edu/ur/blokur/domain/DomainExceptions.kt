package pl.edu.ur.blokur.domain

class UseCaseNotImplementedException(className: String?) : Exception(className + " is not implemented")