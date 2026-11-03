import { useEffect } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { PublicFooter, PublicHeader } from '../components/PublicChrome.jsx'
import { BankCard } from '../components/ui.jsx'

const services = [
  {
    title: 'Savings account',
    text: 'Open an account online with just your name, email and a password. Every new account starts with ₹5,000.',
  },
  {
    title: 'Send money by email',
    text: 'Pay anyone with an ArthGrid account using only their email address. No account number needed.',
  },
  {
    title: 'Instant email alerts',
    text: 'The sender gets a debit alert and the receiver gets a credit alert as soon as a transfer completes.',
  },
  {
    title: 'Complete statement',
    text: 'Every rupee in and out is recorded in your statement, from your opening balance onwards.',
  },
]

const steps = [
  { title: 'Fill in your details', text: 'Name, email and a password. Phone, address and date of birth are optional.' },
  { title: 'Set a transaction PIN', text: 'Choose a 4-digit PIN. You will enter it to approve every transfer.' },
  { title: 'Start banking', text: 'Your account number is ready immediately, with ₹5,000 to start with.' },
]

const protections = [
  ['Encrypted passwords', 'Passwords and PINs are stored as BCrypt hashes. Nobody, including the bank, can read them.'],
  ['PIN on every transfer', 'Money leaves your account only after you confirm the transfer with your 4-digit PIN.'],
  ['Hold, then capture', 'The amount is reserved first and only taken once the receiver is credited. If anything fails, it is released back.'],
  ['Alerts on every movement', 'An email is sent for every debit and credit, so you notice anything unexpected straight away.'],
  ['Automatic logout', 'Net banking signs you out after 5 minutes without activity.'],
  ['Checked on the server', 'Balance checks and amount validation happen in the backend, not just in your browser.'],
]

const services2 = [
  ['API Gateway', 'Single entry point that routes every request'],
  ['Customer service', 'Sign-up, login, profile and PIN'],
  ['Auth service', 'Creates the user identity'],
  ['Accounts service', 'Balances, holds and the ledger'],
  ['Payment service', 'Transfers: hold, credit, capture'],
  ['Notification service', 'Reads Kafka events and sends email'],
]

const team = [
  ['Sumit Bhadoriya', '23T7069'],
  ['Uday Gandhi', '23T7072'],
  ['Avirishu Petwal', '23T7085'],
  ['Jainam Singhai', '23T7026'],
]

export default function Home({ user }) {
  const { hash } = useLocation()
  useEffect(() => {
    if (hash) document.getElementById(hash.slice(1))?.scrollIntoView()
  }, [hash])

  return (
    <div className="public">
      <PublicHeader user={user} />

      <section className="hero">
        <div className="wrap hero-inner">
          <div className="hero-copy">
            <h1>Everyday banking, without the queue.</h1>
            <p>
              Open a savings account in two minutes, start with a ₹5,000 balance, and send money to
              anyone on ArthGrid using just their email address.
            </p>
            <div className="hero-actions">
              <Link className="btn btn-accent btn-lg" to="/open-account">Open an account</Link>
              <Link className="btn btn-outline-light btn-lg" to={user ? '/banking' : '/login'}>Log in to net banking</Link>
            </div>
          </div>
          <div className="hero-visual" aria-hidden="true">
            <BankCard name="Asha Verma" number="5770507171" />
            <div className="hero-receipt">
              <div className="hero-receipt-row">
                <span>Transfer to Ravi Kumar</span>
                <b>₹1,000.00</b>
              </div>
              <div className="hero-receipt-status">
                <span className="dot" /> Successful. Email alerts sent to both accounts.
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="services" className="section">
        <div className="wrap">
          <h2>What you can do with ArthGrid</h2>
          <div className="service-row">
            {services.map((s) => (
              <div className="service" key={s.title}>
                <h3>{s.title}</h3>
                <p>{s.text}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="section section-tint">
        <div className="wrap split">
          <div>
            <h2>Open your account in three steps</h2>
            <p className="lead">No branch visit and no paperwork. You can start using your account straight away.</p>
            <Link className="btn btn-primary" to="/open-account">Start now</Link>
          </div>
          <ol className="steps">
            {steps.map((s) => (
              <li key={s.title}>
                <h3>{s.title}</h3>
                <p>{s.text}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      <section id="security" className="section">
        <div className="wrap">
          <h2>How we protect your account</h2>
          <p className="lead">Security is built into every layer, from the login screen to the database.</p>
          <dl className="protections">
            {protections.map(([t, d]) => (
              <div key={t}>
                <dt>{t}</dt>
                <dd>{d}</dd>
              </div>
            ))}
          </dl>
        </div>
      </section>

      <section id="architecture" className="section section-dark">
        <div className="wrap">
          <h2>How ArthGrid works behind the scenes</h2>
          <p className="lead">
            Instead of one large application, ArthGrid is split into independent Spring Boot microservices.
            Critical steps like moving money happen immediately over REST. Notifications travel separately
            through Apache Kafka, so a slow email never holds up a payment.
          </p>
          <div className="flow">
            <div className="flow-line">
              <span className="flow-node">React website</span>
              <span className="flow-arrow" />
              <span className="flow-node">API Gateway</span>
              <span className="flow-arrow" />
              <span className="flow-node">Customer, Accounts, Payment services</span>
              <span className="flow-arrow" />
              <span className="flow-node">MySQL</span>
            </div>
            <div className="flow-label">Synchronous: the request waits for the answer</div>
            <div className="flow-line">
              <span className="flow-node">Payment completed</span>
              <span className="flow-arrow" />
              <span className="flow-node flow-node-accent">Kafka</span>
              <span className="flow-arrow" />
              <span className="flow-node">Notification service</span>
              <span className="flow-arrow" />
              <span className="flow-node">Email to both people</span>
            </div>
            <div className="flow-label">Asynchronous: sent in the background</div>
          </div>
          <div className="svc-grid">
            {services2.map(([n, d]) => (
              <div key={n}>
                <b>{n}</b>
                <span>{d}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="about" className="section">
        <div className="wrap split">
          <div>
            <h2>About this project</h2>
            <p className="lead">
              ArthGrid is a cloud-based distributed banking and financial transaction platform, developed by
              Group 13 of B.Tech IV Year, E&amp;TC, Section A, at the Institute of Engineering &amp; Technology, DAVV.
            </p>
            <p className="guide">Project guide: <b>Dr. Madhvi Jangalwa</b></p>
          </div>
          <ul className="team">
            {team.map(([n, r]) => (
              <li key={r}>
                <span className="team-initials">{n.split(' ').map((p) => p[0]).join('')}</span>
                <span>
                  <b>{n}</b>
                  <small>{r}</small>
                </span>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="cta-band">
        <div className="wrap cta-inner">
          <h2>Ready to try it?</h2>
          <Link className="btn btn-accent btn-lg" to="/open-account">Open an account</Link>
        </div>
      </section>

      <PublicFooter />
    </div>
  )
}
