import { useState } from "react";
import "./App.css";

import { getRandomBackground } from "./backgrounds";
import { config } from "../config";

function App() {
	const [backgroundImage] = useState(getRandomBackground);
	const [url, setUrl] = useState("");
	const [shortCode, setShortCode] = useState("");
	const [isCustomCodeEnabled, setIsCustomCodeEnabled] = useState(false);
	const [expiresAt, setExpiresAt] = useState("");
	const [isExpirationEnabled, setIsExpirationEnabled] = useState(false);
	const [shortenedUrl, setShortenedUrl] = useState("");
	const [status, setStatus] = useState("");
	const [isSubmitting, setIsSubmitting] = useState(false);

	function getExpirationValue(offset) {
		const date = new Date();
		if (offset.seconds) {
			date.setSeconds(date.getSeconds() + offset.seconds);
		} else if (offset.minutes) {
			date.setMinutes(date.getMinutes() + offset.minutes);
		} else if (offset.days) {
			date.setDate(date.getDate() + offset.days);
		} else if (offset.months) {
			date.setMonth(date.getMonth() + offset.months);
		} else if (offset.years) {
			date.setFullYear(date.getFullYear() + offset.years);
		} else {
			date.setTime(date.getTime() + offset.hours * 60 * 60 * 1000);
		}
		return new Date(date.getTime() - date.getTimezoneOffset() * 60000)
			.toISOString()
			.slice(0, 19);
	}

	async function handleSubmit(event) {
		event.preventDefault();

		setStatus("");
		setShortenedUrl("");
		setIsSubmitting(true);

		console.log(`🌐 Base Url : ${config.apiBaseUrl}`);

		try {
			const uri = new URL(config.apiBaseUrl);
			uri.searchParams.set("u", url);

			if (shortCode.trim()) {
				uri.searchParams.set("shortCode", shortCode.trim());
			}
 
			if (expiresAt) {
                let expiryDate = new Date(expiresAt).toISOString();

				let nowDate = new Date();
				let futureDate = new Date(expiresAt);
				if(futureDate > nowDate) {
					uri.searchParams.set("expire", expiryDate);

				}
				else throw new Error("Expiration Date must be of future.");
	
			}

			const response = await fetch(uri, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
			});

			if (!response.ok) {
				// Try to get the actual error message from the server
				let message = "Unable to shorten that URL.";

				try {
					const errorData = await response.json();
					message =
						errorData.message || errorData.error || errorData.detail || message;
				} catch {
					// Response wasn't JSON
				}

				throw new Error(message);
			}

			const data = await response.json();

			const result = data.shortenedUrl || data.shortUrl || data.url;

			if (!result) {
				throw new Error("The server returned no shortened URL.");
			}

			setShortenedUrl(result);
		} catch (error) {
			console.error("Shortening failed:", error);
			console.log("Error message:", error.message);

			setStatus(error.message);
		} finally {
			setIsSubmitting(false);
		}
	}

	async function handleCopy() {
		await navigator.clipboard.writeText(shortenedUrl);
		setStatus("Copied to clipboard.");
	}

	return (
		<main
			className="app-shell"
			style={{ backgroundImage: `url(${backgroundImage})` }}>
			<section className="shortener" aria-labelledby="page-title">
				<h1 id="page-title" className="ubuntu-medium ">
					Shortly
				</h1>
				<p className="intro">Turn long URLs into clean, easy-to-share links.</p>

				<form className="shorten-form" onSubmit={handleSubmit}>
					<label htmlFor="long-url">Enter your long URL</label>
					<div className="input-row">
						<input
							id="long-url"
							type="url"
							value={url}
							onChange={(event) => setUrl(event.target.value)}
							placeholder="eg: https://en.wikipedia.org/wiki/Earth#/"
							required
						/>
						{!isCustomCodeEnabled && !isExpirationEnabled && (
							<button type="submit" disabled={isSubmitting}>
								{isSubmitting ? "Shortening..." : "Shorten"}
							</button>
						)}
					</div>

					{!isCustomCodeEnabled ? (
						<button
							className="custom-code-toggle"
							type="button"
							onClick={() => setIsCustomCodeEnabled(true)}>
							Want a custom shortened code?
						</button>
					) : (
						<div className="custom-code-field">
							<label htmlFor="short-code">Custom short code</label>
							<div className="input-row">
								<input
									id="short-code"
									type="text"
									value={shortCode}
									onChange={(event) => setShortCode(event.target.value)}
									placeholder="eg: shortly-home"
									pattern="[A-Za-z0-9_-]+"
									maxLength={32}
									title="Use only letters, numbers, hyphens, or underscores."
									required
								/>
								{!isExpirationEnabled && (
									<button type="submit" disabled={isSubmitting}>
										{isSubmitting ? "Shortening..." : "Shorten"}
									</button>
								)}
							</div>
							<span>Use letters, numbers, hyphens, or underscores.</span>
						</div>
					)}

					<br />

					{!isExpirationEnabled ? (
						<button
							className="custom-code-toggle"
							type="button"
							onClick={() => setIsExpirationEnabled(true)}>
							Want this link to expire?
						</button>
					) : (
						<div className="expiration-field">

							<label htmlFor="expires-at">Expiration date and time</label>
							<div
								className="expiration-presets"
								aria-label="Expiration shortcuts">
								<button
									type="button"
									onClick={() =>
										setExpiresAt(getExpirationValue({ seconds: 5 }))
									}>
									5 seconds
								</button>
								<button
									type="button"
									onClick={() =>
										setExpiresAt(getExpirationValue({ minutes: 1 }))
									}>
									1 minute
								</button>
								<button
									type="button"
									onClick={() =>
										setExpiresAt(getExpirationValue({ minutes: 30 }))
									}>
									30 minutes
								</button>
								<button
									type="button"
									onClick={() =>
										setExpiresAt(getExpirationValue({ hours: 1 }))
									}>
									1 hour
								</button>

								<button
									type="button"
									onClick={() => setExpiresAt(getExpirationValue({ days: 1 }))}>
									1 day
								</button>

								<button
									type="button"
									onClick={() =>
										setExpiresAt(getExpirationValue({ hours: 168 }))
									}>
									1 week
								</button>
								<button
									type="button"
									onClick={() =>
										setExpiresAt(getExpirationValue({ months: 1 }))
									}>
									1 month
								</button>
								<button
									type="button"
									onClick={() =>
										setExpiresAt(getExpirationValue({ years: 1 }))
									}>
									1 year
								</button>
							</div>
							<div className="input-row">
								<input
									id="expires-at"
									type="datetime-local"
									value={expiresAt}
									onChange={(event) => setExpiresAt(event.target.value)}
									min={new Date().toISOString().slice(0, 19)}
									step="1"
									required
								/>
								<button type="submit" disabled={isSubmitting}>
									{isSubmitting ? "Shortening..." : "Shorten"}
								</button>
							</div>
							<span>Choose when this shortened URL should stop working.</span>
						</div>
					)}
				</form>

				{shortenedUrl && (
					<div className="result" aria-live="polite">
						<div>
							<span className="result-label">Shortened URL</span>
							<a href={shortenedUrl} target="_blank" rel="noreferrer">
								{shortenedUrl}
							</a>
						</div>
						<button className="copy-button" type="button" onClick={handleCopy}>
							Copy
						</button>
					</div>
				)}

				{status && (
					<p className="status" role="status">
						{status}
					</p>
				)}
			</section>
			<footer>
				<span>Fast, focused, and free to use.</span> <span className="divider">|</span>
				<span aria-label="Privacy information">
					Your recent searches are not saved.
				</span>
			</footer>
		</main>
	);
}

export default App;
