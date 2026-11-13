const $ = (s, root=document) => root.querySelector(s);
const $$ = (s, root=document) => [...root.querySelectorAll(s)];
let models = [
    {id:1,name:"GPT-4",description:"Modèle langage ultra-performant, capable de comprendre et générer du texte de manière avancée.",provider:"OpenAI",active:true,logo:""},
    {id:2,name:"Llama 3",description:"Modèle open source polyvalent développé par Meta.",provider:"Meta",active:true,logo:""},
    {id:3,name:"Mistral Large",description:"Modèle performant et efficace pour les tâches complexes.",provider:"Mistral AI",active:false,logo:""}
];
let nextId=4, editingId=null, selectedId=null, logoData="";
const pages={chat:"Chatbot",models:"Modèles d’IA",form:"Modèle d’IA",detail:"Détails du modèle",settings:"Paramètres",about:"À propos"};
function toast(msg){const el=$("#toast");el.textContent=msg;el.classList.add("show");clearTimeout(toast.timer);toast.timer=setTimeout(()=>el.classList.remove("show"),2600)}
function navigate(page){$$(".page").forEach(p=>p.classList.add("hidden"));$("#page-"+page)?.classList.remove("hidden");$$(".nav-item").forEach(b=>b.classList.toggle("active",b.dataset.page===page));$("#pageLabel").textContent=pages[page]||"Modèle";$(".sidebar").classList.remove("open");if(page==="models")renderModels();window.scrollTo(0,0)}
function logoMarkup(model, extra=""){return model.logo?`<div class="model-logo ${extra}"><img src="${model.logo}" alt="Logo ${esc(model.name)}"></div>`:`<div class="model-logo ${extra}">${model.name.toLowerCase().includes("llama")?"🦙":model.name.toLowerCase().includes("mistral")?"🌬":"✺"}</div>`}
function esc(s=""){return String(s).replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]))}
function renderModels(){const q=$("#searchModels").value.toLowerCase(),f=$("#statusFilter").value;const list=models.filter(m=>(m.name+" "+m.description+" "+m.provider).toLowerCase().includes(q)&&(f==="all"||(f==="active"&&m.active)||(f==="inactive"&&!m.active)));$("#modelRows").innerHTML=list.map(m=>`<tr><td><div class="model-cell">${logoMarkup(m)}<span>${esc(m.name)}</span></div></td><td class="description-cell">${esc(m.description)}</td><td><span class="pill ${m.active?"active":"inactive"}">${m.active?"Actif":"Inactif"}</span></td><td><div class="actions"><button class="action-btn" title="Voir" data-action="view" data-id="${m.id}">◉</button><button class="action-btn" title="Modifier" data-action="edit" data-id="${m.id}">✎</button><button class="action-btn delete" title="Supprimer" data-action="delete" data-id="${m.id}">⌫</button></div></td></tr>`).join("");$("#emptyState").classList.toggle("hidden",list.length>0);$("#totalCount").textContent=models.length;$("#activeCount").textContent=models.filter(m=>m.active).length;$("#inactiveCount").textContent=models.filter(m=>!m.active).length}
function openForm(id=null){editingId=id;const m=id?models.find(x=>x.id===id):null;$("#formTitle").textContent=m?"Modifier un modèle":"Créer un modèle";$("#modelName").value=m?.name||"";$("#modelDescription").value=m?.description||"";$("#modelProvider").value=m?.provider||"";$("#modelActive").checked=m?m.active:true;logoData=m?.logo||"";$("#logoPreview").innerHTML=logoData?`<img src="${logoData}" alt="Aperçu logo">`:(m? (m.name.toLowerCase().includes("llama")?"🦙":m.name.toLowerCase().includes("mistral")?"🌬":"✺"):"✺");$("#activeLabel").textContent=$("#modelActive").checked?"Actif":"Inactif";navigate("form")}
function showDetail(id){selectedId=id;const m=models.find(x=>x.id===id);if(!m)return;$("#detailCard").innerHTML=`${logoMarkup(m,"detail-logo")}<div class="detail-content"><h2>${esc(m.name)} <span class="pill ${m.active?"active":"inactive"}">${m.active?"Actif":"Inactif"}</span></h2><p>${esc(m.description)}</p><div class="detail-meta"><div><span>Fournisseur</span><b>${esc(m.provider||"Non renseigné")}</b></div><div><span>Créé le</span><b>${m.created||"13/11/2026"}</b></div><div><span>Identifiant</span><b>AI-${String(m.id).padStart(4,"0")}</b></div></div><div class="form-actions"><button class="btn btn-outline" data-page="models">Retour</button><button class="btn btn-primary" id="detailEdit">✎ Modifier</button></div></div>`;navigate("detail");$("#detailEdit").onclick=()=>openForm(id)}
$("#loginForm").addEventListener("submit",e=>{e.preventDefault();$("#loginView").classList.add("hidden");$("#appView").classList.remove("hidden");navigate("chat");toast("Connexion de démonstration réussie")});
$("#demoLogin").onclick=()=>$("#loginForm").requestSubmit();$("#forgotLink").onclick=e=>{e.preventDefault();toast("Fonctionnalité non connectée dans cette maquette")};
$("#logout").onclick=()=>{$("#appView").classList.add("hidden");$("#loginView").classList.remove("hidden");toast("Vous êtes déconnecté")};
$$("[data-page]").forEach(b=>b.addEventListener("click",()=>navigate(b.dataset.page)));
$("#newModel").onclick=()=>openForm();$("#searchModels").oninput=renderModels;$("#statusFilter").onchange=renderModels;
$("#modelRows").addEventListener("click",e=>{const b=e.target.closest("[data-action]");if(!b)return;const id=Number(b.dataset.id);if(b.dataset.action==="view")showDetail(id);if(b.dataset.action==="edit")openForm(id);if(b.dataset.action==="delete"){const m=models.find(x=>x.id===id);if(confirm(`Supprimer le modèle « ${m.name} » ?`)){models=models.filter(x=>x.id!==id);renderModels();toast("Modèle supprimé")}}});
$("#modelActive").onchange=()=>$("#activeLabel").textContent=$("#modelActive").checked?"Actif":"Inactif";
$("#logoInput").onchange=e=>{const file=e.target.files[0];if(!file)return;if(file.size>2*1024*1024){toast("Le fichier dépasse 2 Mo");e.target.value="";return}const reader=new FileReader();reader.onload=()=>{logoData=reader.result;$("#logoPreview").innerHTML=`<img src="${logoData}" alt="Aperçu logo">`};reader.readAsDataURL(file)};
$("#modelForm").onsubmit=e=>{e.preventDefault();const data={name:$("#modelName").value.trim(),description:$("#modelDescription").value.trim(),provider:$("#modelProvider").value.trim()||"Non renseigné",active:$("#modelActive").checked,logo:logoData};if(editingId){models=models.map(m=>m.id===editingId?{...m,...data}:m);toast("Modèle mis à jour")}else{models.unshift({id:nextId++,...data,created:"13/11/2026"});toast("Modèle créé avec succès")}navigate("models");e.target.reset();logoData=""};
$("#mobileMenu").onclick=()=>$(".sidebar").classList.toggle("open");
$("#chatForm").onsubmit=e=>{e.preventDefault();sendMessage()};
$("#chatInput").onkeydown=e=>{if(e.key==="Enter"&&!e.shiftKey){e.preventDefault();sendMessage()}};
function sendMessage() {
    const messages = ["Je suis une maquette de démonstration. Connectez un modèle IA via une API pour obtenir des réponses réelles."]
    const length = messages.length
    const input = $("#chatInput")
        , text = input.value.trim();
    if (!text)
        return;
    appendMessage(text, "user");
    input.value = "";
    setTimeout( () => appendMessage(messages[Math.floor(Math.random()*length)], "assistant"), 450)
}
function appendMessage(text, type) {
    const row = document.createElement("div");
    row.className = "message " + type;
    row.innerHTML = type === "assistant" ? `<div class="bot-avatar">✺</div><div class="bubble"></div>` : `<div class="bubble"></div>`;
    row.querySelector(".bubble").textContent = text;
    $("#messages").appendChild(row);
    $("#messages").scrollTop = $("#messages").scrollHeight
}
$$(".suggestion").forEach(b=>b.onclick=()=>{$("#chatInput").value=b.textContent.replace(/^✦\s*/,"");$("#chatInput").focus()});
// Initial view
renderModels();