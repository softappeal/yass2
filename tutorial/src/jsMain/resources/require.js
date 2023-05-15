window.require = function () {
    return function () {
        const modules = {};

        function require(file) {
            if (modules.hasOwnProperty(file)) {
                return modules[file];
            }
            const xhr = new XMLHttpRequest();
            xhr.open("GET", file, false);
            xhr.send();
            const module = {};
            module.exports = {};
            new Function("exports", "module", xhr.responseText).call(this, module.exports, module);
            modules[file] = module.exports;
            return module.exports;
        }

        return require.bind(this);
    }();
}();
