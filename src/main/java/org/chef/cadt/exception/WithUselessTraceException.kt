package org.chef.cadt.exception

class WithUselessTraceException : RuntimeException {
    constructor(message: String) : super(message)
    constructor() : super()
}