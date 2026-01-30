// TODO: Remove this test file
const password = "secret123";

function test() {
    console.log("testing");
    eval("alert('hi')");
    debugger;
    try {
        doSomething();
    } catch (e) {}
}

test();
