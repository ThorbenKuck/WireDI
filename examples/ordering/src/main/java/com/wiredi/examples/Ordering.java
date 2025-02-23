package com.wiredi.examples;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;

interface Base {
}

@Wire
@Order(before = Second.class)
class First implements Base {
}

@Wire
@Order(before = Third.class)
class Second implements Base {
}

@Wire
@Order(3)
class Third implements Base {
}

@Wire
@Order(after = Third.class)
class Fourth implements Base {
}

@Wire
@Order(5)
class Fifth implements Base {
}