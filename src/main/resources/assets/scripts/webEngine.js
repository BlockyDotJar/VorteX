var boxBody = document.getElementsByClassName('Box-body')[0];
var container = document.createElement('div');

container.className = 'content-container';
container.style.padding = '20px';
container.innerHTML = boxBody.innerHTML;

document.body.innerHTML = '';
document.body.appendChild(container);

var anchors = document.querySelectorAll('.content-container a');

anchors.forEach(function(anchor) {
    anchor.addEventListener('click', function(event) {
        event.preventDefault();
    });
});

var elementsToRemove = document.querySelectorAll('.content-container .details-reset.details-overlay.position-relative');

elementsToRemove.forEach(function(element) {
    element.parentNode.removeChild(element);
});

window.setTimeout(function() {
    window.scrollTo(0, 0);
}, 1);
